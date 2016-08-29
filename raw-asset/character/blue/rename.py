import os
import glob

print "currently in folder", os.getcwd()

dirname = os.path.split(os.path.abspath(os.getcwd()))[1]

list_files = glob.glob("*.png")
print list_files

new_names = []
for name in list_files:
    buff = name.split("_")
    if dirname not in buff:
        buff.insert(1, dirname)
    #new_names.append("_".join(buff))
    os.rename(name, "_".join(buff))

##print new_names
