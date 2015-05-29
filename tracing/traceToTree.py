import csv

relations = {}
children = {}

def dumpChildren(key, path="> "):
	if key not in relations:
		return
	mypath = path + key[0:4]
	cs = relations[key]
	for c in cs:
		print "%s '%s' (%s ms)" % (mypath, children[c][0], children[c][1])
		dumpChildren(c, mypath + " > ")

def main():
	with open('raw.txt', 'rb') as csvfile:
		reader = csv.reader(csvfile, delimiter='|')
		for row in reader:
			if len(row) != 7:
				continue
			row = [ x.strip() for x in row ]
			(parent, child, desc, dur) = row[2:6]
			children[child] = [ desc, dur ]
			if parent in relations:
				relations[parent].append(child)
			else:
				relations[parent] = [ child ]

	head = None
	for r in relations.keys():
		if r not in children:
			head = r

	if not head:
		assert False, "No head node found!"
	else:
		dumpChildren(head)

main()
