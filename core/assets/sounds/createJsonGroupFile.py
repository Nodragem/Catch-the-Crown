import os, re, sys
import json, glob, shutil
import subprocess as sp

input_path = "D:\\Google Drive\\Art and Creation\\Jeux-Prog\\SoundEffect"
output_path = "D:\\Google Drive\\Art and Creation\\Jeux-Prog\\RopeGame\\core\\assets\\sounds"
sox = "D:\\Program Files (x86)\\sox-14-4-2\\sox.exe"

print "Input files from:", input_path
print "Ouput files to:", output_path

## clean up
output_files = glob.glob(output_path+"\\*.wav")
print output_files
for f in output_files:
    os.remove(f)

inputfilelist = glob.glob(input_path+"\\OldSounds\\*.wav") + glob.glob(input_path+"\\NewSounds\\*.wav")
i = 0
for filename in inputfilelist:
    basename = os.path.basename(filename)
    if basename[0] == "X" or basename[0] == "x":
        print "Don't include: ", basename
        continue
    newbasename = "".join([letter if not letter.isupper() else "_"+letter.lower() if i>0 else letter.lower() for i, letter in enumerate(basename)])
    print "\nCopy:", filename
    print "to :", output_path+"\\"+newbasename
    # shutil.copy(filename, output_path+"\\"+newbasename)
    # -- instead of shutil we use sox to copy and convert to the right format
    command = [sox, filename,
               "--bits", "16", output_path+"\\"+newbasename]
    print "sox" + ' '.join(command[1:]) + ''
    sp.call(command)



dico = {}
foldername =  output_path.split("\\")[-1]
correctFiles = [name for name in os.listdir(output_path) if re.search("^\w*_\d*.wav", name)]
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
