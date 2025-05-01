## [1.1.2](https://github.com/nicolasfara/experiments-2025-acm-iot-ac-llm/compare/v1.1.1...v1.1.2) (2025-05-01)

### Bug Fixes

* fix gemma's models names ([e505e3e](https://github.com/nicolasfara/experiments-2025-acm-iot-ac-llm/commit/e505e3e7f37ea9d98dbb264e977a8086ae6eb21b))

### Build and continuous integration

* check styling rules ([23f4cb1](https://github.com/nicolasfara/experiments-2025-acm-iot-ac-llm/commit/23f4cb152af4728608d94762d0a14f7fd5f9ad3e))

### Style improvements

* reformat code ([75b185d](https://github.com/nicolasfara/experiments-2025-acm-iot-ac-llm/commit/75b185d0d0b4b965ec27147b872d9a4bd98ae301))

### Refactoring

* minor code refactor ([a5dec9b](https://github.com/nicolasfara/experiments-2025-acm-iot-ac-llm/commit/a5dec9bb72de5afef7a3c5afdc5e34949df9645f))

## [1.1.1](https://github.com/nicolasfara/experiments-2025-acm-iot-ac-llm/compare/v1.1.0...v1.1.1) (2025-05-01)

### Bug Fixes

* minors in generated data ([994bcb1](https://github.com/nicolasfara/experiments-2025-acm-iot-ac-llm/commit/994bcb18d5015ab4a04515703701e1687925836b))

## [1.1.0](https://github.com/nicolasfara/experiments-2025-acm-iot-ac-llm/compare/v1.0.0...v1.1.0) (2025-05-01)

### Features

* implement pass@k metric and export CSV table with results ([96db598](https://github.com/nicolasfara/experiments-2025-acm-iot-ac-llm/commit/96db59828c29f25bc73e301e73a5133118b91ed7))

## 1.0.0 (2025-05-01)

### Features

* add gemini factory ([2617801](https://github.com/nicolasfara/experiments-2025-acm-iot-ac-llm/commit/261780101628f66de42aafa592470018187a369b))
* add gemini service ([21ddabd](https://github.com/nicolasfara/experiments-2025-acm-iot-ac-llm/commit/21ddabddb98e9e16f36817f6557ebdaeda08f53f))
* add logging capabilities to the experiment ([7405d22](https://github.com/nicolasfara/experiments-2025-acm-iot-ac-llm/commit/7405d22168434495dc83759eee24a67ae9a288a6))
* add Ollama service ([9452b59](https://github.com/nicolasfara/experiments-2025-acm-iot-ac-llm/commit/9452b592998fa4c901fdd7465798ada7f4066834))
* channel with obstacle ([13a04e2](https://github.com/nicolasfara/experiments-2025-acm-iot-ac-llm/commit/13a04e299a3823834bea9a50bf5d71388dd54580))
* first working experiments for the channel ([1f9d04d](https://github.com/nicolasfara/experiments-2025-acm-iot-ac-llm/commit/1f9d04d12c2f7d23f51a7de64fcb82811c767bf1))
* integrate all the test cases into the appropriate package ([b66038f](https://github.com/nicolasfara/experiments-2025-acm-iot-ac-llm/commit/b66038f8559433d505530e032245fd995c7f1574))
* parametrize knowledge to inject in queries ([1be3d68](https://github.com/nicolasfara/experiments-2025-acm-iot-ac-llm/commit/1be3d6803ec88b76dd987d6200ce3427230dff6b))
* switch to cats-effect ([8b01237](https://github.com/nicolasfara/experiments-2025-acm-iot-ac-llm/commit/8b01237269fadb0e03fef34d5a4fc91fc992f451))
* try to add the knowledge with G information ([dfa6a22](https://github.com/nicolasfara/experiments-2025-acm-iot-ac-llm/commit/dfa6a22c69ed272a09dd8c72a712fc09d8264b75))
* try to make the findParent work (with long description, uhm) ([f86a7d3](https://github.com/nicolasfara/experiments-2025-acm-iot-ac-llm/commit/f86a7d3ba73cf37752c643dfb1f059d101a16db3))

### Bug Fixes

* minor and fulle test execution ([7350178](https://github.com/nicolasfara/experiments-2025-acm-iot-ac-llm/commit/735017871e4da9bc333935e0ea6295840f33838c))
* remove unused imports ([16e7f9f](https://github.com/nicolasfara/experiments-2025-acm-iot-ac-llm/commit/16e7f9f728763fbe816f483ea57c19e204087bc0))

### Tests

* add channel program ([27d3de4](https://github.com/nicolasfara/experiments-2025-acm-iot-ac-llm/commit/27d3de43e098320bd17894091fa9ad9762e16729))
* minors on test ([6d499a7](https://github.com/nicolasfara/experiments-2025-acm-iot-ac-llm/commit/6d499a738da61a20effb3e7d0a7a4166d96ceae5))

### Build and continuous integration

* add circe dependencies ([69b8dc8](https://github.com/nicolasfara/experiments-2025-acm-iot-ac-llm/commit/69b8dc830b375b25dc329f8c26a8419321bac679))
* add retry library ([5cb1374](https://github.com/nicolasfara/experiments-2025-acm-iot-ac-llm/commit/5cb1374a52878c5a8d4ad5dbd7f4b9597a254beb))
* move fork at top-level setting ([158aaa1](https://github.com/nicolasfara/experiments-2025-acm-iot-ac-llm/commit/158aaa16b143c485bd71f74bf74833e2fc9c4f73))

### Refactoring

* remove ProgramLoder trait ([6e659eb](https://github.com/nicolasfara/experiments-2025-acm-iot-ac-llm/commit/6e659eb6b9051f84f8703d6490d632b9c5ad4101))
* rewrite gemini provider implementation ([bbaadca](https://github.com/nicolasfara/experiments-2025-acm-iot-ac-llm/commit/bbaadca1817fc515b3eb04b9f47290dc6aabcd32))
* use new strategy for loading prompts from file ([89b1e70](https://github.com/nicolasfara/experiments-2025-acm-iot-ac-llm/commit/89b1e705e94283b3b97bf25977b1c3254a0dc328))

## [1.1.1](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/compare/v1.1.0...v1.1.1) (2024-12-10)

### Dependency updates

* **core-deps:** update dependency scala to v3.6.2 ([7e0382a](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/7e0382ab9252ff2655b04e0238ab539bcd0e39c5))
* **deps:** update dependency semantic-release-preconfigured-conventional-commits to v1.1.116 ([5653109](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/56531092a3e6f00f3f8bd78160273b461e92adb3))

## [1.1.0](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/compare/v1.0.3...v1.1.0) (2024-12-03)

### Features

* add scalafix and new scalafmt config ([4166cbd](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/4166cbd599a65bacfcd69f571982ecfc2ba9b385))

### Dependency updates

* **deps:** update dependency com.github.sbt:sbt-ci-release to v1.9.0 ([097744a](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/097744accc9a1b673d990b44fe8986d7fb840d48))
* **deps:** update dependency org.scala-native:sbt-scala-native to v0.5.6 ([8ef5416](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/8ef541601ceebc1114cfc24a2405aaf38fd4b722))
* **deps:** update dependency sbt/sbt to v1.10.4 ([96c7fcf](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/96c7fcfc14f2af0786bb1541aca0c59f6e06f369))
* **deps:** update dependency sbt/sbt to v1.10.5 ([7b6d9a8](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/7b6d9a8ce38c1c5408cb7d32eae808a65f8f9914))
* **deps:** update dependency sbt/sbt to v1.10.6 ([46e1b1b](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/46e1b1b78667916be2abedb971f9364b09eb4fe1))
* **deps:** update dependency semantic-release-preconfigured-conventional-commits to v1.1.112 ([0135480](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/01354803d22c9fc5eb668a96896405af1ca6a18a))
* **deps:** update dependency semantic-release-preconfigured-conventional-commits to v1.1.113 ([c8f1fa9](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/c8f1fa9e2f0086b29419238c85932f2c22ad5ce6))
* **deps:** update dependency semantic-release-preconfigured-conventional-commits to v1.1.114 ([25f52aa](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/25f52aa7923a4259208199e491f4b7dda0df7e10))
* **deps:** update dependency semantic-release-preconfigured-conventional-commits to v1.1.115 ([88aa986](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/88aa98605c419cf130cffc53719913aa3d649b7c))

### Build and continuous integration

* **deps:** update actions/checkout action to v4.2.2 ([a975947](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/a975947987305ffbb7f9e60d62accd27e9f7d838))
* **deps:** update actions/setup-node action to v4.1.0 ([959bb33](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/959bb33e4f77d09d0d9557a081e8d0795504ecf5))
* **deps:** update nicolasfara/build-check-deploy-sbt-action action to v1.0.11 ([b2334d3](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/b2334d33f1564f493e923242e946012f495216e9))
* **deps:** update nicolasfara/build-check-deploy-sbt-action action to v1.0.12 ([2b0ae95](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/2b0ae95cfbbee56ef77650c2c1fd75aea4abef0c))
* **deps:** update nicolasfara/build-check-deploy-sbt-action action to v1.0.13 ([893d987](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/893d987ddaf3275fe7291c6ee560ca53fde5a05e))

## [1.0.3](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/compare/v1.0.2...v1.0.3) (2024-10-21)

### Dependency updates

* **core-deps:** update dependency scala to v3.6.1 ([22a52dd](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/22a52dd84fd8870cfefc2deab2e004ff3b19f24b))
* **deps:** update dependency com.github.sbt:sbt-ci-release to v1.7.0 ([b2246be](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/b2246be0e93af9b3f19f4a9312fb5fbda6455438))
* **deps:** update dependency com.github.sbt:sbt-ci-release to v1.8.0 ([32e1333](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/32e1333e2ccb4355e5fcd9b4881deed691e50ba5))
* **deps:** update dependency org.scala-js:sbt-scalajs to v1.17.0 ([b916ec1](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/b916ec163744fd15de0e4a30b8432f11eaf8fef2))
* **deps:** update dependency sbt/sbt to v1.10.3 ([78d9303](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/78d93031a4fa3f0503db4807f99d08674d070f67))
* **deps:** update dependency semantic-release-preconfigured-conventional-commits to v1.1.107 ([d15d115](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/d15d115de6726f58ea28e98bb032ab48e6f0be58))
* **deps:** update dependency semantic-release-preconfigured-conventional-commits to v1.1.108 ([4017f81](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/4017f81d90ea7136af40e8afe30164d0e5cb521c))
* **deps:** update dependency semantic-release-preconfigured-conventional-commits to v1.1.109 ([33a83fa](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/33a83fa4a5e82d4fa2cc397fa767cad7bedf0e68))
* **deps:** update dependency semantic-release-preconfigured-conventional-commits to v1.1.110 ([d3b0e6f](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/d3b0e6f4276f2df529846a8edaf45f24b7ff2f21))
* **deps:** update dependency semantic-release-preconfigured-conventional-commits to v1.1.111 ([e60163e](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/e60163e30cdbb872f9e8106ac2afe67bc757ed06))
* **deps:** update node.js to 20.18 ([9251792](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/92517920a60024e884c4849b9c0b3c79a2e64d24))

### Build and continuous integration

* call check task ([8103570](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/81035702e6d006e879a43aa61f7f245881005e0c))
* **deps:** update actions/checkout action to v4.2.0 ([164f204](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/164f204e78abb7467d1d7bb3d10d2a646a7bb203))
* **deps:** update actions/checkout action to v4.2.1 ([11bde0f](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/11bde0f18abb5b5b26858784a9d54d17730bcbb6))
* **deps:** update actions/setup-node action to v4.0.4 ([87374f4](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/87374f44f0fc666a64ce3682b964108958b4b010))
* **deps:** update dependency ubuntu to v24 ([b8b034b](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/b8b034bc2b1447775e83c31cb84514ba8be28448))
* **deps:** update nicolasfara/build-check-deploy-sbt-action action to v1.0.10 ([ff39162](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/ff391622400c33fa245323dd6e5840f41bc35a0b))
* **deps:** update nicolasfara/build-check-deploy-sbt-action action to v1.0.5 ([ab90266](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/ab90266c0302a6e820c2c2a5d0579dc54e2663c2))
* **deps:** update nicolasfara/build-check-deploy-sbt-action action to v1.0.6 ([c22ed99](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/c22ed99296d9fb55b73507e5d6d52f91927faf46))
* **deps:** update nicolasfara/build-check-deploy-sbt-action action to v1.0.7 ([b849da8](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/b849da8fdcecafa07fd158147db4e4ec1f3e5ef8))
* **deps:** update nicolasfara/build-check-deploy-sbt-action action to v1.0.8 ([d5bb616](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/d5bb616400bccffa0c2a0b297b1a406f7a6af3dd))
* **deps:** update nicolasfara/build-check-deploy-sbt-action action to v1.0.9 ([97c354b](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/97c354bed753004e38f71735fc15d1d7a260b626))
* set codecov token explicit ([dcfe3b7](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/dcfe3b7bb73568902fa03f6b2f88f8407c39a30e))
* setup scalafmt and check task ([e1215f0](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/e1215f0cc53ed52d4229e75eb5f4583104915e9a))

### General maintenance

* add gitattributes ([2d613ef](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/2d613efa10f025da6ec7b780a19f0f025e2fab47))

### Style improvements

* add missing newline ([fb59ef8](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/fb59ef8541940a5005a21d31db5b4a31665651d2))

## [1.0.2](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/compare/v1.0.1...v1.0.2) (2024-09-20)

### Dependency updates

* **core-deps:** update dependency scala to v3.5.1 ([3191258](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/31912583071ddbec16cd9d2b45d06fe95dca7be9))

## [1.0.1](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/compare/v1.0.0...v1.0.1) (2024-09-20)

### Dependency updates

* **core-deps:** update dependency scala to v3.5.0 ([01fbb4c](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/01fbb4c387d8a0fb324a4f1dc9a19f02f7651cb8))
* **deps:** update dependency com.github.sbt:sbt-ci-release to v1.6.0 ([26eb06d](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/26eb06dd26339e70928263d4cde21860450ad433))
* **deps:** update dependency com.github.sbt:sbt-ci-release to v1.6.1 ([0df31ba](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/0df31bae32dce7785a87e8f45a0f8961e7249a3e))
* **deps:** update dependency org.scala-native:sbt-scala-native to v0.5.5 ([cc0bae4](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/cc0bae44f1e0dfd9c61bd45526d6e5db82b972d4))
* **deps:** update dependency sbt/sbt to v1.10.1 ([7b05bb8](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/7b05bb88347843666f63d31228fb3ffcb2a71ce7))
* **deps:** update dependency sbt/sbt to v1.10.2 ([ea8b417](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/ea8b4175afcdc8da5d9ad25e4320e11c06bdbbe4))
* **deps:** update dependency semantic-release-preconfigured-conventional-commits to v1.1.100 ([61240c0](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/61240c025b620e7aab2de1404d187df1af406c9b))
* **deps:** update dependency semantic-release-preconfigured-conventional-commits to v1.1.101 ([49b8698](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/49b86983df3816e0597213f6ba8056b9feaa71be))
* **deps:** update dependency semantic-release-preconfigured-conventional-commits to v1.1.102 ([c66d538](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/c66d538d578f2073c5655f63dc7948d3f7a105fb))
* **deps:** update dependency semantic-release-preconfigured-conventional-commits to v1.1.104 ([f2c8484](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/f2c848496046bdc8228dddb1969d35c73e6b85f2))
* **deps:** update dependency semantic-release-preconfigured-conventional-commits to v1.1.105 ([6ec20bb](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/6ec20bb813753b69f291584f212a481b80859727))
* **deps:** update dependency semantic-release-preconfigured-conventional-commits to v1.1.106 ([853e1fa](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/853e1fad4fdd44cbfacdcd63905a25c401d93841))
* **deps:** update dependency semantic-release-preconfigured-conventional-commits to v1.1.91 ([6049539](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/60495391ddb00756558bbadb94a6d7f342689544))
* **deps:** update dependency semantic-release-preconfigured-conventional-commits to v1.1.92 ([7a8cb2d](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/7a8cb2dc31a086c7252314958582157055ffbc10))
* **deps:** update dependency semantic-release-preconfigured-conventional-commits to v1.1.93 ([0965332](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/09653320bcb51d1531e581efaf4ef3bfdc85f48a))
* **deps:** update dependency semantic-release-preconfigured-conventional-commits to v1.1.95 ([dbfb53c](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/dbfb53c8f754e0d193b556a60059f34fc412d88e))
* **deps:** update dependency semantic-release-preconfigured-conventional-commits to v1.1.96 ([a86d2c6](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/a86d2c6088b766d77f245a4a50add5cd8a8ef94d))
* **deps:** update dependency semantic-release-preconfigured-conventional-commits to v1.1.97 ([f0a4277](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/f0a4277ae9f44a0a3b8b5ba57c6fc7bd877bd247))
* **deps:** update dependency semantic-release-preconfigured-conventional-commits to v1.1.99 ([26b464f](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/26b464f6dc418d954e37b70c3f827e5f29e2372a))
* **deps:** update node.js to 20.16 ([f9a2a8c](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/f9a2a8c97742fd5f4defa162bce848780a1a55b7))
* **deps:** update node.js to 20.17 ([a30ed2b](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/a30ed2b7577c580a4efacaebb2cd787eedce1ac6))

### Build and continuous integration

* **deps:** update nicolasfara/build-check-deploy-sbt-action action to v1.0.2 ([6c4dac2](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/6c4dac241340936107c671f9de4556ef15ceff22))
* **deps:** update nicolasfara/build-check-deploy-sbt-action action to v1.0.3 ([898a5b9](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/898a5b9d9e1e59d7ccefa7b088be0f8d91cb6088))
* **deps:** update nicolasfara/build-check-deploy-sbt-action action to v1.0.4 ([1a05353](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/1a05353390f1a5c244bf3f8ea305918089444a32))

### General maintenance

* add mergify config ([87819d1](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/87819d1c66129e0b4edc50145d3cdd1201775bf3))

## 1.0.0 (2024-07-20)

### Features

* add semantif-release config file ([3bad011](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/3bad011b4d648d02f5f5813636e43c2009443e4a))
* first draft multiplatform code ([2c90d98](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/2c90d98fa08a33225ef3577751b8e116feb7972f))

### Dependency updates

* **deps:** add renovate.json ([#1](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/issues/1)) ([2c4a9b4](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/2c4a9b474fdb6a0d60bac0364c5c2bdeb22bbef8))

### Build and continuous integration

* add ci ([301fd88](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/301fd88d482e07b8c0fb6e33a743a040dae7dc2f))
* add different target triple ([4e1d62c](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/4e1d62cc33d89e92505b6fd9ad1749252e4878af))
* add publish plugin and build metadata ([3a93b38](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/3a93b382160cb882b6c98856855bc489970c5ae5))
* bump version ([d632f99](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/d632f9935c83649bc4dd71a727e10263a21b1614))
* disable triple target ([3ea2e20](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/3ea2e20651daa1ce3461c1d36da0bcc547bdb112))
* enable different compilations for macos ([9de94f2](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/9de94f29bc8753631e02b5b5e941f2c5de27c9d4))
* fail fast on release, no retry ([ae60cd4](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/ae60cd40e38ab9b6ae0b7080963f5699bb39bda7))
* prevent publishing on test ([f0bc6f5](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/f0bc6f5e4699ddb3e542ea56dc7232232ac31b2c))
* set to default linking option ([f154584](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/f154584b3225bced5c99c32e282528a9ba9980e8))
* use sonatype s01 host ([fb50571](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/fb50571b8296c62f58404a5f89336224122419b4))

### General maintenance

* add gitignore ([797b92f](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/797b92fe076dfb4c98e53b88553d920af62952a3))
* add release command for sbt ([5c9c02f](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/5c9c02f2c13ce6c20e9e62487f065f0d926d0fc8))
* add scala-sepcific config ([dea9d4f](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/dea9d4ff4d0dfc6cbcaf07fd42bbfa4345611ca4))
* change tag format ([6379b49](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/6379b49da7644fbf56a198868acad45397d8860e))
* **release:** 1.0.0 [skip ci] ([95457cd](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/95457cd289e3a630c7ad6c7f9cf3f4ab08f24d4a)), closes [#1](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/issues/1)

## 1.0.0 (2024-07-17)

### Features

* add semantif-release config file ([3bad011](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/3bad011b4d648d02f5f5813636e43c2009443e4a))
* first draft multiplatform code ([2c90d98](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/2c90d98fa08a33225ef3577751b8e116feb7972f))

### Dependency updates

* **deps:** add renovate.json ([#1](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/issues/1)) ([2c4a9b4](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/2c4a9b474fdb6a0d60bac0364c5c2bdeb22bbef8))

### Build and continuous integration

* add ci ([301fd88](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/301fd88d482e07b8c0fb6e33a743a040dae7dc2f))
* add different target triple ([4e1d62c](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/4e1d62cc33d89e92505b6fd9ad1749252e4878af))
* add publish plugin and build metadata ([3a93b38](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/3a93b382160cb882b6c98856855bc489970c5ae5))
* bump version ([d632f99](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/d632f9935c83649bc4dd71a727e10263a21b1614))
* disable triple target ([3ea2e20](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/3ea2e20651daa1ce3461c1d36da0bcc547bdb112))
* enable different compilations for macos ([9de94f2](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/9de94f29bc8753631e02b5b5e941f2c5de27c9d4))
* prevent publishing on test ([f0bc6f5](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/f0bc6f5e4699ddb3e542ea56dc7232232ac31b2c))
* set to default linking option ([f154584](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/f154584b3225bced5c99c32e282528a9ba9980e8))

### General maintenance

* add gitignore ([797b92f](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/797b92fe076dfb4c98e53b88553d920af62952a3))
* add release command for sbt ([5c9c02f](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/5c9c02f2c13ce6c20e9e62487f065f0d926d0fc8))
* add scala-sepcific config ([dea9d4f](https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects/commit/dea9d4ff4d0dfc6cbcaf07fd42bbfa4345611ca4))
