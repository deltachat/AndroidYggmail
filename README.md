# AndroidYggmail

This is a port of [Yggmail](https://github.com/neilalexander/yggmail) with a basic user interface for Android.
AndroidYggmail is an email companion app that allows you to send email within the p2p-network [Yggdrasil](https://yggdrasil-network.github.io/).
You can use AndroidYggmail in conjunction with the email client of your choice, however we recommend using it with DeltaChat as we actively work on a seamless integration.
Please read the [introduction](https://github.com/neilalexander/yggmail#introduction) to learn more about the reasoning behind Yggmail.

We don't have released the app yet and we consider it to be in an alpha stage. 
However you can build AndroidYggmail by yourself and try it already out.

## Check out repository and build AndroidYggmail

AndroidYggmail uses a git submodule to fetch and cross-compile the yggmail code for Android.
To checkout the repository you can do:

```
   git clone https://github.com/deltachat/AndroidYggmail.git
   git submodule update --init --recursive
```
Next, change to the lib-yggmail directory:
```
   cd AndroidYggmail/lib-yggmail
```
You can now build the yggmail library. The script currently only supports Linux and Mac. 
If you're using Windows and are able to adapt the build script accordingly, please send us a pull request :)
```
  ./build_core.sh
```

You're almost done! If you have Android Studio installed, you can now just hit the play button and you'll get your development version of Yggmail.
Command line users who __have set up the Android SDK correctly__ can simply type from the repository's root directory
```
./gradlew clean assembleRelease
```
 
## Contributing

Contributions are very welcome. 
If you find bugs, please create tickets on our issue tracker and if you have patches for them we're even happier.
We haven't set up yet a project on transifex, but if you're interested in translations, please let us know.


# Credits

This app relies heavily on the great work of [Neil Alexander](https://github.com/neilalexander/) and the Yggdrasil community!

# License

Licensed GPLv3+, see the LICENSE file for details.

Copyright © 2021 Delta Chat contributors.
