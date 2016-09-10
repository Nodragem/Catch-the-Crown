import glob, os, shutil
import subprocess as sp
from PIL import Image

pallettes = [text.split('\\')[1] for text in glob.glob("./pallette_*.ase")]
colours =  [text.split('_')[1].split('.')[0] for text in pallettes]
output_path = ".\\output2"
aseprite = "D:\\Google Drive\\Art and Creation\\Aseprite-v1.1.7\\Aseprite.exe"
if not os.path.exists(output_path):
    os.mkdir(output_path)
else:
    print  "Erase all files ..."
    [os.remove(f) for f in glob.glob(output_path+"/*")]

## export the character layer:
for i, pallette in enumerate(pallettes):
    command = [aseprite, "-b",
                 "--layer", "character",
                "Piaf_animation03_"+colours[i]+"toExport.ase",
                "--filename-format", '{path}/{title}{layer}_{tag}_{tagframe01}.{extension}',
                "--save-as", output_path+"\\Piaf_"+colours[i]+".png"]
    sp.call(command)
    command[0] = 'aseprite'
    print ' '.join(command) + '\n'

## export the Lance/Slapping/Carrying layers:
layers = ["Carrying", "Slapping", "Throwing_Lance"]
layers_name = ["Carrying", "Slapping", "Lance"]
for j, layer in enumerate(layers):
    for i, pallette in enumerate(pallettes):
        command = [aseprite, "-b",
                        "Piaf_animation03_"+colours[i]+"toExport.ase",
                        "--layer", layer,
                        "--frame-tag", "Walking",
                        "--filename-format", '{path}/{title}_{tag}_{tagframe01}.{extension}',
                        "--save-as", output_path+"\\Arms_"+layers_name[j]+"_"+colours[i]+".png"]
        sp.call(command)
        command[0] = 'aseprite'
        print ' '.join(command) + '\n'

[os.remove(f) for f in glob.glob(output_path+"/Arms_*") if Image.open(f).getcolors(1) is not None]
[os.rename(s, s.replace("Walking", "")) for s in glob.glob(output_path+"/Arms_*") if "Walking" in s]

## export the Golden Halo FX:
