import subprocess as sp
import os

os.chdir("D:\\Google Drive\\Art and Creation\\Jeux-Prog\\RopeGame\\raw-asset\\GUI")

output_path = ".\\"
aseprite = "D:\\Google Drive\\Art and Creation\\Aseprite-v1.1.7\\Aseprite.exe"

command = [aseprite, "-b",
            "buttons_smaller.ase",
            "--filename-format", '{path}/{title}_{tag}_{tagframe01}.{extension}',
            "--save-as", output_path+"\\buttons.png"]
sp.call(command)

command[0] = 'aseprite'
print ' '.join(command) + '\n'
