import os

path_in = "Test"
path_out = "Train"

folders = os.listdir(path_in)

for folder in folders:
    sub_dir = os.listdir(os.path.join(path_in, folder))
    for file in sub_dir:
        old_name = os.path.join(path_in, folder, file)
        new_name = os.path.join(path_out, folder, file)
        os.rename(old_name, new_name)
