import subprocess as sp

output_path = ".\\"
aseprite = "D:\\Google Drive\\Art and Creation\\Aseprite-v1.1.7\\Aseprite.exe"

command = [aseprite, "-b",
            "*.ase",
            "--sheet-pack",
            "--sheet", output_path+"\\jungle_tiles.png"]
sp.call(command)

command[0] = 'aseprite'
print ' '.join(command) + '\n'

# similar to: aseprite -b *.ase --sheet-pack --sheet atlas.png
