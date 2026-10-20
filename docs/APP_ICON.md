# App icon

The launcher icon uses a white account/wallet outline and coin with a mint outward arrow on a dark navy background. The arrow represents requesting a cash out; it does not indicate that a payout has completed.

- drawable/ic_launcher_background.xml: solid navy background (#102B36).
- drawable/ic_launcher_foreground.xml: white wallet/coin and mint arrow (#5EEAD4).
- drawable/ic_launcher_monochrome.xml: single-color silhouette for themed launchers.
- mipmap-anydpi-v26/ic_launcher.xml and ic_launcher_round.xml: adaptive icons, using foreground/background/monochrome resources.
- mipmap-anydpi-v24/ic_launcher.xml and ic_launcher_round.xml: vector fallback for Android 7.x (minimum SDK 24). These anydpi resources take precedence over the original density-specific template raster icons.

Artwork is defined directly as Android vectors, with the central mark kept inside the adaptive icon safe area. Existing manifest icon references remain valid. Launcher appearance on an emulator/device has not been manually verified.

Validation: both uatDebug and prodDebug APK builds passed.
