package com.kwonps.mtouchpos.view.navgraph

sealed interface NavigationGraphState {
    enum class HomeView: NavigationGraphState {
        Home,
        PgIdLogin,
        RegisteredId
    }
    enum class CommonView: NavigationGraphState {
        ItemListDialog,
        MessageDialog,
        CompletePayment
    }
    enum class CreditPaymentView: NavigationGraphState {
        CreditPayment,
        PaymentProcessDialog
    }
    enum class DeviceSettingView: NavigationGraphState {
        Bluetooth,
        USB,
        BluetoothConnectDialog,
        USBConnectDialog
    }
    enum class DirectPaymentView: NavigationGraphState {
        DirectPayment
    }
    enum class PaymentHistoryView: NavigationGraphState {
        PaymentHistory,
        PaymentHistoryDetail,
        PaymentStatistic,
        Calendar
    }
}
