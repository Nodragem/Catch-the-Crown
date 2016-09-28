import subprocess as sp
import glob, shutil, os
from PIL import Image

listfile = glob.glob("./*.png")
for filename in listfile:
    if "crown" not in filename and "ground_shock" not in filename:
        os.remove(filename)

output_path = ".\\"
aseprite = "D:\\Google Drive\\Art and Creation\\Aseprite-v1.1.7\\Aseprite.exe"

command = [aseprite, "-b",
            "lance_animation.ase",
           "--trim",
            "--filename-format", '{path}/{title}{tag}_{tagframe01}.{extension}',
            "--save-as", output_path+"\\.png"]
sp.call(command)

command[0] = 'aseprite'
print ' '.join(command) + '\n'

## export the Lance/Slapping/Carrying layers:
colours = ["yellow", "green", "blue", "purple", "red"]

listfile = glob.glob("./Attack*.png")
for filename in listfile:
    image = Image.open(filename)
    image.load()
    w, h = image.size
    cropped = image.crop([0,0, 32, h]).save(filename)
    for colour in colours:
        arr = filename.split("_")
        arr.insert(1, colour)
        newname = "_".join(arr).replace("\\", "/")
        print newname
        shutil.copy2(filename, newname)
    os.remove(filename)


print listfile
