package dji.sampleV5.aircraft.api

class APIClient {
    fun provideDeviceStreamEndpoints(): DeviceStreamAPI = RetrofitService.getInstance(
        baseUrl = "https://www.demo.spyskytech.com:5000/",
        interceptors = listOf()
    )
}