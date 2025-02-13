package com.adeasy.advertise.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.adeasy.advertise.model.Order_Item;
import com.adeasy.advertise.model.User;

/**
 * Created by Manuka yasas,
 * University Sliit
 * Email manukayasas99@gmail.com
 **/
public class BuynowViewModel extends ViewModel {
    private MutableLiveData<User> customer = new MutableLiveData<>();
    private MutableLiveData<Order_Item> item = new MutableLiveData<>();
    private MutableLiveData<Boolean> validateCustomerDetails = new MutableLiveData<>();
    private MutableLiveData<Boolean> mobileNumberVerifyStatus = new MutableLiveData<>();
    private MutableLiveData<Boolean> paymentSelectCOD = new MutableLiveData<>();
    private MutableLiveData<Boolean> visibilityContinue = new MutableLiveData<>();

    public LiveData<User> getCustomer() {
        return customer;
    }

    public void setCustomer(User order_customer) {
        customer.setValue(order_customer);
    }

    public LiveData<Order_Item> getItem() {
        return item;
    }

    public void setItem(Order_Item item) {
        this.item.setValue(item);
    }

    public LiveData<Boolean> getValidateCustomerDetails() {
        return validateCustomerDetails;
    }

    public void setValidateCustomerDetails(Boolean validateCustomerDetails) {
        this.validateCustomerDetails.setValue(validateCustomerDetails);
    }

    public LiveData<Boolean> getMobileNumberVerifyStatus() {
        return mobileNumberVerifyStatus;
    }

    public void setMobileNumberVerifyStatus(Boolean mobileNumberVerifyStatus) {
        this.mobileNumberVerifyStatus.setValue(mobileNumberVerifyStatus);
    }

    public LiveData<Boolean> getPaymentSelectCOD() {
        return paymentSelectCOD;
    }

    public void setPaymentSelectCOD(Boolean paymentSelectCOD) {
        this.paymentSelectCOD.setValue(paymentSelectCOD);
    }

    public LiveData<Boolean> getVisibilityContinue() {
        return visibilityContinue;
    }

    public void setVisibilityContinue(Boolean visibilityContinue) {
        this.visibilityContinue.setValue(visibilityContinue);
    }

}
