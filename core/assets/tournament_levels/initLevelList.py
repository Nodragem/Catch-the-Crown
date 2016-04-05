import os, glob
import json

#with open("progressionLevels.json", "w+") as file:
#	jsonfile = json.load(file)
jsonfile = {}
jsonfile["levels"] = []
jsonfile["unblocked"] = []
for i, namefile in enumerate(glob.glob("./level_*.tmx")):
	print namefile
	jsonfile["levels"].append(os.path.splitext(os.path.basename(namefile))[0] )
	if i == 0:
		jsonfile["unblocked"].append(True)
	else:
		jsonfile["unblocked"].append(False)
	
with open("progressionLevels.json", "w+") as file:
	json.dump(jsonfile, file)
	