package org.example.BankBridge.Database.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import org.example.BankBridge.App;
import org.example.BankBridge.Database.model.Account;
import org.example.BankBridge.Database.model.User;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Contains necessary database (CRUD) operations
 */
public class FirebaseService {

    public boolean registerUser(User user) {
        // Create user request with only email/phone num for authentication
        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail(user.email())
                .setPhoneNumber(user.phoneNum())
                .setDisabled(false);

        UserRecord record;
        try {
            record = App.fauth.createUser(request);
            addNewUserToDb(user, record.getUid());
            return true;
        } catch (FirebaseAuthException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void addNewUserToDb(User user, String uuid) {
        DocumentReference docRef = App.fstore.collection("users").document(UUID.randomUUID().toString());

        Map<String, Object> userData = dataMap(
                "account_type", "CLIENT",
                "uuid", uuid,
                "first_name", user.firstName(),
                "last_name", user.lastName(),
                "email", user.email(),
                "phone_num", user.phoneNum(),
                "password", user.password()
        );

        //asynchronously write data
        ApiFuture<WriteResult> result = docRef.set(userData);
    }

    public void addNewBankAccountToDb(Account account) {
        DocumentReference docRef = App.fstore.collection("accounts").document(UUID.randomUUID().toString());

        Map<String, Object> accountData = dataMap(
                "account_type", "CHECKING",
                "account_number", account.accountNumber(),
                "balance", account.balance(),
                "date_created", account.dateCreated()
        );

        //asynchronously write data
        ApiFuture<WriteResult> result = docRef.set(accountData);
    }


    // Helper method for easier insertion of data in the form of Map<String, Object>
    private Map<String, Object> dataMap(Object... keyValuePairs) {
        if (keyValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException("Invalid number of arguments. Must be key-value pairs.");
        }

        Map<String, Object> data = new HashMap<>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            String key = String.valueOf(keyValuePairs[i]);
            Object value = keyValuePairs[i + 1];
            data.put(key, value);
        }
        return data;
    }


}