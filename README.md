# BeautyGanServingApp

< 개발 환경 >
- Window
- Python 3.8
- Package 
  (1) Flask 
  (2) Android Studio(Kotlin)
  (3) BeautyGan
  
< 동작 시나리오 >
- 합성하고자 하는 Base 얼굴을 Application 상에서 선택한다.
- 합성하고자 하는 Reference 얼굴을 Application 상에서 선택한다.
- Base 얼굴과 Reference 얼굴을 Flask Web Server로 전달한다.
- BeautyGan Model을 퉁해 두개의 얼굴을 합성하고 결과를 Callback 한다.
- Callback한 데이터를 Dialog로 Application에 출력한다.
