package com.example.mtouchpos.view.navgraph

sealed interface NavigationGraphState {
    enum class HomeView: NavigationGraphState {
        Home,
        Login,
        PgIdLogin,
        VanIdLogin,
        RegisteredId
    }
    enum class CommonView: NavigationGraphState {
        ItemListDialog,
        ErrorDialog,
        CompletePayment,
        LoadingDialog
    }
    enum class CreditPaymentView: NavigationGraphState {
        CreditPayment,
        BluetoothPaymentDialog,
        UsbPaymentDialog
    }
    enum class DeviceSettingView: NavigationGraphState {
        Bluetooth,
        USB
    }
    enum class DirectPaymentView: NavigationGraphState {
        DirectPayment
    }
    enum class PaymentHistoryView: NavigationGraphState {
        PaymentHistory,
        PaymentHistoryDetail,
        Calendar
    }
}
