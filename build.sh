#!/bin/bash
bash gradlew assembleDebug --no-daemon --info

#Check if build on Termux using ugly way
if [ $PREFIX == "/data/data/com.termux/files/usr" ] ; then
  echo "Termux detected! Checking root access..."
  if [ !su ] ; then
    echo "No root detected! Installing the app using non-root method..."
    termux-open ./app/build/outputs/apk/debug/SaaF-Android-debug.apk
    echo "App installed"
  else
    echo "Root detected! Installing the app using root method"
    tsudo pm install ./app/build/outputs/apk/debug/SaaF-Android-debug.apk
    echo "App installed"
  fi
  confirm() {
    read -r -p "${1:-You want to open the app? [y/N] (default: y)} " response
    case "$response" in
      [yY][eE][sS]|[yY]) 
        true
        ;;
      [nN][oO][nN])
        false
        ;;
      *)
        true
        ;;
    esac
  }
  confirm && am start --user 0 -n com.aozoradev.saaf/com.aozoradev.saaf.MainActivity
fi
