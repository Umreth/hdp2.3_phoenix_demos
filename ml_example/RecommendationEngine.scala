import org.apache.phoenix.spark._ 

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.rdd._
import org.apache.spark.mllib.recommendation.{ALS, Rating, MatrixFactorizationModel}

import org.apache.log4j.Logger
import org.apache.log4j.Level

Logger.getLogger("org").setLevel(Level.ERROR)
Logger.getLogger("akka").setLevel(Level.ERROR)

def computeRmse(model: MatrixFactorizationModel, data: RDD[Rating], n: Long): Double = {
	val predictions: RDD[Rating] = model.predict(data.map(x => (x.user, x.product)))
	val predictionsAndRatings = predictions.map{
		x => ((x.user, x.product), x.rating)
	}.join(data.map(x => ((x.user, x.product), x.rating))).values

	math.sqrt(predictionsAndRatings.map(x => (x._1 - x._2) * (x._1 - x._2)).mean())
}

// Raw movie data.
val movie_rdd: RDD[Map[String, AnyRef]] = sc.phoenixTableAsRDD(
	"MOVIES",
	Seq("MOVIEID", "TITLE", "GENRES"),
	zkUrl = Some("localhost:2181:/hbase-unsecure")
)

val rate_rdd: RDD[Map[String, AnyRef]] = sc.phoenixTableAsRDD(
	"RATINGS",
	Seq("USERID", "MOVIEID", "RATING"),
	zkUrl = Some("localhost:2181:/hbase-unsecure")
)

// We will ignore any movie that has not been rated at least 5 times.
val minRatings = 5
val movieCounts = rate_rdd.map(x => (x("MOVIEID").asInstanceOf[Int], 1)).groupByKey().map(x => (x._1, x._2.size))
val nReviews = movieCounts.collect().toMap

// Transform the ratings table into Rating objects.
val ratings = rate_rdd.
	filter( x => nReviews(x("MOVIEID").asInstanceOf[Int]) > minRatings ).
	map(x => (
		Rating(
			x("USERID").asInstanceOf[Int],
			x("MOVIEID").asInstanceOf[Int],
			x("RATING").asInstanceOf[Int].toDouble
		)))

// Get the number of users and number of movies.
val numUsers = ratings.map(_.user).distinct.count
val numMovies = ratings.map(_.product).distinct.count

// Partition into training, validation and test datasets.
val splits = ratings.randomSplit(Array(0.6, 0.2, 0.2))
val training   = splits(0).cache()
val validation = splits(1).cache()
val test       = splits(2).cache()
val numValidation = validation.count()
val numTest = test.count()

// Find a decent predictive model.
val ranks = List(4, 8, 12)
val lambdas = List(0.01, 0.1, 1.0)
val numIters = List(10, 20, 30)
var bestModel: Option[MatrixFactorizationModel] = None
var bestValidationRmse = Double.MaxValue
var bestRank = 0
var bestLambda = -1.0
var bestNumIter = -1
for (rank <- ranks; lambda <- lambdas; numIter <- numIters) {
	val model = ALS.train(training, rank, numIter, lambda)
	val validationRmse = computeRmse(model, validation, numValidation)
	println("RMSE (validation) = " + validationRmse + " for the model trained with rank = "
		+ rank + ", lambda = " + lambda + ", and numIter = " + numIter + ".")
	if (validationRmse < bestValidationRmse) {
		bestModel = Some(model)
		bestValidationRmse = validationRmse
		bestRank = rank
		bestLambda = lambda
		bestNumIter = numIter
	}
}

// Compute top 5 recommendations for each user.
// Start with a cross product of all users to movies as an RDD.
val crossProduct = sc.parallelize(
	(for (x <- 1 to numUsers.toInt; y <- 1 to numMovies.toInt) yield (x, y))
)

// Make the predictions. Filter out low recommendations.
val predictions = bestModel.get.predict(crossProduct).filter(x => x.rating > 3.4)

// Group by user and sort by predicted score within that user group.
// Keep only the top 5 predictions.
val grouped = predictions.map(x => (x.user, (x.product, x.rating))).groupByKey()
val sorted = grouped.map( x => (
	x._1,
	x._2.toList.sortBy(- _._2).map(x => x._1).padTo(5, 0).take(5)
))

// Interactive Example: What are the top 5 movies for user 20?
// Build a hash of movie ID to name.
// val movieMap = movie_rdd.map(
//	x => x("MOVIEID").asInstanceOf[Int] -> x("TITLE").asInstanceOf[String]
// ).collect().toMap
// val recommendations = sorted.sortByKey().collect()
// "Recommendations for user 20"
// val temp = recommendations(20)
// temp._2.map(x => movieMap(x))

// Store the recommendations back to Phoenix.
"Saving values to Phoenix"
val recTuples = sorted.map(x => x match { case (a, List(b, c, d, e, f)) => (a, b, c, d, e, f) })
recTuples.saveToPhoenix( "RECOMMENDATIONS",
	Seq("USERID", "REC1", "REC2", "REC3", "REC4", "REC5"),
	zkUrl = Some("localhost:2181:/hbase-unsecure")
)

// Script ends
// Debug: Top 5 movies by rating.
//val groupedByMovie = ratings.map(x => (x.product, (x.rating))).groupByKey()
//val movieAverages = groupedByMovie.map(x => ( x._1, x._2.sum / x._2.size )).sortBy(- _._2)
