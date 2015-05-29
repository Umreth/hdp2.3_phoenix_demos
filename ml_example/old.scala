import org.apache.phoenix.spark._ 
import org.apache.spark.rdd.RDD
val sqlCtx = new org.apache.spark.sql.SQLContext(sc)

val rdd: RDD[Map[String, AnyRef]] = sc.phoenixTableAsRDD(
  "PRICES", Seq("SYMBOL","DATE","TIME", "OPEN","HIGH","LOW","CLOSE","VOLUME"), zkUrl = Some("localhost:2181:/hbase-unsecure")
)
rdd.count()
