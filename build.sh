#!/bin/bash
trap "exit" INT
set -e

bash gradlew assembleDebug --no-daemon --info

#Check if build on Termux using ugly way
if [ $PREFIX == "/data/data/com.termux/files/usr" ] ; then
  echo "Termux detected! Checking root access..."
  if [ !su ] ; then
    echo "No root detected! Installing the app using non-root method..."
    termux-open ./app/build/outputs/apk/debug/SaaF-Android.apk
    echo "App installed"
  else
    echo "Root detected! Installing the app using root method"
    tsudo pm install ./app/build/outputs/apk/debug/SaaF-Android.apk
    echo "App installed"
  fi
  confirm() {
    read -p "You want to open the app? [y/N] " response
    case "$response" in
      [yY]*) 
        am start --user 0 -n com.aozoradev.saaf/com.aozoradev.saaf.MainActivity
        ;;
      [nN]*)
        exit 1
        ;;
      *)
        echo "No response. Skipping the app opener..."
        ;;
    esac
  }
  confirm
fi
