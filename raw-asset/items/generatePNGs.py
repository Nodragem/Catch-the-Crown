import subprocess as sp

output_path = ".\\"
aseprite = "D:\\Google Drive\\Art and Creation\\Aseprite-v1.1.7\\Aseprite.exe"

command = [aseprite, "-b",
            "lance_animation.ase",
           "--trim",
            "--filename-format", '{path}/{title}_{tag}_{tagframe01}.{extension}',
            "--save-as", output_path+"\\Lance.png"]
sp.call(command)

command[0] = 'aseprite'
print ' '.join(command) + '\n'
