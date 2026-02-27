# Known bugs
- Music player
```bash
Feb 27, 2026 10:47:11 PM net.sourceforge.jaad.mp4.api.Track readNextFrame
WARNING: readNextFrame failed: frame 45 already skipped, offset:169,129, stream:6,819,481
[22:47:11] [Sigma-Music-3/ERROR] (Sigma) Failed to play track
 java.io.IOException: frame already skipped and no random access
	at net.sourceforge.jaad.mp4.api.Track.readNextFrame(Track.java:336) ~[jaad-1.0.2.jar:?]
	at io.github.sst.remake.manager.impl.MusicManager.streamAudioData(MusicManager.java:520) ~[main/:?]
	at io.github.sst.remake.manager.impl.MusicManager.playTrack(MusicManager.java:467) ~[main/:?]
	at io.github.sst.remake.manager.impl.MusicManager.playbackLoop(MusicManager.java:399) [main/:?]
	at io.github.sst.remake.manager.impl.MusicManager.lambda$initPlaybackLoop$0(MusicManager.java:363) [main/:?]
	at java.lang.Thread.run(Thread.java:1447) [?:?]

Feb 25, 2026 12:15:05 PM net.sourceforge.jaad.mp4.api.Track readNextFrame
WARNING: readNextFrame failed: tried to read 371 bytes at 3,421,887
[12:15:05] [Sigma-Music-3/ERROR] (Sigma) Failed to play track
java.io.EOFException: null
at net.sourceforge.jaad.mp4.MP4InputStream.read(MP4InputStream.java:195) ~[jaad-1.0.2.jar:?]
at net.sourceforge.jaad.mp4.MP4InputStream.readBytes(MP4InputStream.java:275) ~[jaad-1.0.2.jar:?]
at net.sourceforge.jaad.mp4.api.Track.readNextFrame(Track.java:342) ~[jaad-1.0.2.jar:?]
at io.github.sst.remake.manager.impl.MusicManager.streamAudioData(MusicManager.java:501) ~[main/:?]
at io.github.sst.remake.manager.impl.MusicManager.playTrack(MusicManager.java:475) ~[main/:?]
at io.github.sst.remake.manager.impl.MusicManager.playbackLoop(MusicManager.java:408) ~[main/:?]
at io.github.sst.remake.manager.impl.MusicManager.lambda$initPlaybackLoop$1(MusicManager.java:372) ~[main/:?]
at java.lang.Thread.run(Thread.java:1583) [?:?]
Feb 25, 2026 12:16:26 PM net.sourceforge.jaad.mp4.api.Track readNextFrame
WARNING: readNextFrame failed: tried to read 371 bytes at 3,421,887
[12:16:26] [Sigma-Music-4/ERROR] (Sigma) Failed to play track
java.io.EOFException: null
at net.sourceforge.jaad.mp4.MP4InputStream.read(MP4InputStream.java:195) ~[jaad-1.0.2.jar:?]
at net.sourceforge.jaad.mp4.MP4InputStream.readBytes(MP4InputStream.java:275) ~[jaad-1.0.2.jar:?]
at net.sourceforge.jaad.mp4.api.Track.readNextFrame(Track.java:342) ~[jaad-1.0.2.jar:?]
at io.github.sst.remake.manager.impl.MusicManager.streamAudioData(MusicManager.java:501) ~[main/:?]
at io.github.sst.remake.manager.impl.MusicManager.playTrack(MusicManager.java:475) ~[main/:?]
at io.github.sst.remake.manager.impl.MusicManager.playbackLoop(MusicManager.java:408) ~[main/:?]
at io.github.sst.remake.manager.impl.MusicManager.lambd
```