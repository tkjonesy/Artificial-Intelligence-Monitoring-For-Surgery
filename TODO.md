## To do

- [x] Work on breaking the current CameraFetcher method into 4 threads
  - [x] Thread 1: Grab the frame
  - [x] Thread 2: Make inference and draw the frame
  - [x] Thread 3: Display to GUI
  - [x] Thread 4: Write the frame to the recording
- [x] Modify FileSession
  - [x] Use a backup codec if FileWriter fails to open
  - [x] Resize all frames to a certain width and height when writing to video
- [ ] Find new method for grabbing camera devices on Windows
- [ ] Take another look at mini arr gen to see if there is a better way to do it
- [ ] Add a setting to change inference log colors