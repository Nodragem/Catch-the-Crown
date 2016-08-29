# -*- coding: utf-8 -*-
"""
Created on Sat Mar 21 16:38:07 2015

@author: Geoffrey
"""

import os, shutil, glob

print "start from", os.getcwd()

root_path = os.getcwd()
toavoid = ["topack", "level_tiles"]

## clean up
filelist = glob.glob(root_path+"\\topack\\*.png")
print filelist
for f in filelist:
    os.remove(f)

i = 0
for (path, dirs, files) in os.walk(root_path):
    dirname = os.path.split(os.path.abspath(path))[1]
    if dirname in toavoid or dirname[0] == "X":
        print "Avoid: ", path
        continue
    print path
    print "Directories found:"
    print dirs
    filtered_files = [f for f in files if ".png" in f]
    filtered_files = [f for f in filtered_files if f[0] != "X"]
    print "PNG Files found:"    
    print filtered_files
    print "----"
    for srcfile in filtered_files:
        shutil.copy("\\".join([path,srcfile]), root_path+"\\topack")
    