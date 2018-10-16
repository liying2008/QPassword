# QPassword (七圈密码)

[![GitHub release](https://img.shields.io/github/release/liying2008/QPassword.svg)](https://github.com/liying2008/QPassword/releases)
[![Build Status](https://travis-ci.org/liying2008/QPassword.svg?branch=master)](https://travis-ci.org/liying2008/QPassword)
[![Github Releases](https://img.shields.io/github/downloads/liying2008/QPassword/total.svg)](https://codeload.github.com/liying2008/QPassword/zip/master)
[![license](https://img.shields.io/github/license/liying2008/QPassword.svg)](https://github.com/liying2008/QPassword/blob/master/LICENSE)


> ### [Download Latest APK](https://github.com/liying2008/QPassword/releases/download/v1.0.1/qpassword_v1.0.1.apk)

## Compiling Environment

- compileSdkVersion 28
- targetSdkVersion 28
- Gradle Version 4.6
- Kotlin Version 1.2.71

### Linux

```shell
./gradlew assembleDebug
```

### Windows

```shell
gradlew assembleDebug
```

> **项目可直接导入 <code>Android Studio 3.2</code> 中运行。**

## Primary Function

“**七圈密码**” 是一款用来保存你的各类账号及密码的软件，你只需要记住一个主密码即可轻松管理保存的所有密码，大大减轻了记忆负担。

### Others

1. 如果用户设置了主密码，则用户所保存的密码都会使用AES加密算法进行加密，加密密钥根据主密码生成；如果用户选择不设置主密码，则保存在数据库中的密码数据将会以明文形式存储；
2. 该应用不联网，用户存储的所有密码均保存在本地；
3. 该应用的部分界面参考了：https://github.com/o602075123/MyPassword 。

## ScreenShot

![ScreenShot1](screenshot/1.png)
![ScreenShot2](screenshot/2.png)
![ScreenShot3](screenshot/3.png)
![ScreenShot4](screenshot/4.png)
![ScreenShot5](screenshot/5.png)
![ScreenShot6](screenshot/6.png)

## Update

2018-10-16

## ChangeLog

[点击查看更新日志](CHANGELOG.md)

## Contact Me

CSDN：[http://blog.csdn.net/u012939909](http://blog.csdn.net/u012939909)  
Email：[liruoer2008@yeah.net](mailto:liruoer2008@yeah.net)  

## Thanks

- [fastjson](https://github.com/alibaba/fastjson)
- [PatternLockView](https://github.com/aritraroy/PatternLockView)
- [guava](https://github.com/google/guava)

## License

```
Copyright 2017-2018 独毒火

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

