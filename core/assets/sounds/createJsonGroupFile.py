import os, re, sys
import json

dico = {}
foldername =  os.getcwd().split("\\")[-1]
correctFiles = [name for name in os.listdir("./") if re.search("^\w*_\d*.wav", name)]
with open("soundGroups.json", "wb") as outfile:
    for filename in correctFiles:
        #filename = os.path.basename(filename)
        print filename
        info = filename.split("_")
        groupName = "_".join(info[0:-1])  # remove the number at the end
        if groupName not in dico.keys():
            dico[groupName] = []
        nitem = {}
        nitem["id"] = int(info[-1].split(".")[0])
        nitem["type"] = "com.badlogic.gdx.audio.Sound"
        nitem["path"] = foldername + "/" + filename
        #nitem["parameters"] = {}
        dico[groupName].append(nitem)
    json.dump(dico, outfile, indent=4, sort_keys=True)

