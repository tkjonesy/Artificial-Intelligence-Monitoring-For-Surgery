## To do

- [ ] Work on breaking the current CameraFetcher method into 4 threads
  - [ ] Thread 1: Grab the frame
  - [ ] Thread 2: Make inference and draw the frame
  - [ ] Thread 3: Display to GUI
  - [ ] Thread 4: Write the frame to the recording
- [ ] Modify FileSession
  - [ ] Use a backup codec if FileWriter fails to open
  - [ ] Resize all frames to a certain width and height when writing to video
- [ ] Find new method for grabbing camera devices on Windows
- [ ] Take another look at mini arr gen to see if there is a better way to do it