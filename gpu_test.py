import tensorflow as tf

gpu_available = tf.test.is_gpu_available()
is_cuda_gpu_available = tf.test.is_gpu_available(cuda_only=True)

devices = tf.config.list_physical_devices('GPU')
print(devices)

print("GPU avaiable: " + str(gpu_available))
print("CUDA avaiable: " + str(is_cuda_gpu_available))
