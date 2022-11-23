from firebase_admin import credentials, initialize_app, storage
# Init firebase with your credentials
cred = credentials.Certificate("google-services.json")
initialize_app(cred, {'storageBucket': 'sound-recorder-bc3d7.appspot.com'})

bucket = storage.bucket()

for firebase_obj in bucket.list_blobs():
    file = open(firebase_obj.name, "wb")
    file_data = bucket.get_blob(firebase_obj.name).download_as_bytes()
    file.write(file_data)
    file.close()