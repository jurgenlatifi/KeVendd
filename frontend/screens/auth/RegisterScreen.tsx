import { router } from "expo-router";
import React, { useState } from "react";
import {
  Keyboard,
  StyleSheet,
  TouchableWithoutFeedback,
  View,
} from "react-native";

import AuthButton from "../../components/auth/AuthButton";
import AuthFooter from "../../components/auth/AuthFooter";
import AuthHeader from "../../components/auth/AuthHeader";
import AuthLayout from "../../components/auth/AuthLayout";
import CustomInput from "../../components/auth/CustomInput";
import PasswordInput from "../../components/auth/PasswordInput";

export default function RegisterScreen() {
  const [email, setEmail] = useState("");
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [phone, setPhone] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  const [securePassword, setSecurePassword] = useState(true);
  const [secureConfirmPassword, setSecureConfirmPassword] = useState(true);

  const handleRegister = () => {
    console.log("Register pressed");
  };

  const goToLogin = () => {
    router.push("/login" as any);
  };

  return (
    <TouchableWithoutFeedback onPress={Keyboard.dismiss} accessible={false}>
      <View style={{ flex: 1 }}>
        <AuthLayout>
          <View style={styles.headerContainer}>
            <AuthHeader title="Regjistrohu" compact />
          </View>

          <View style={styles.formContainer}>
            <CustomInput
              value={email}
              onChangeText={setEmail}
              placeholder="E-mail"
              icon="email"
            />

            <CustomInput
              value={firstName}
              onChangeText={setFirstName}
              placeholder="Emri i përdoruesit"
              icon="person-outline"
            />

            <CustomInput
              value={lastName}
              onChangeText={setLastName}
              placeholder="Mbiemri i përdoruesit"
              icon="person-outline"
            />

            <CustomInput
              value={phone}
              onChangeText={setPhone}
              placeholder="Numri i telefonit"
              icon="phone"
            />

            <PasswordInput
              value={password}
              onChangeText={setPassword}
              placeholder="Fjalëkalimi"
              secureTextEntry={securePassword}
              onToggleSecure={() => setSecurePassword(!securePassword)}
            />

            <PasswordInput
              value={confirmPassword}
              onChangeText={setConfirmPassword}
              placeholder="Konfirmo fjalëkalimin"
              secureTextEntry={secureConfirmPassword}
              onToggleSecure={() =>
                setSecureConfirmPassword(!secureConfirmPassword)
              }
            />
          </View>

          <View style={styles.buttonContainer}>
            <AuthButton title="Regjistrohu" onPress={handleRegister} />
          </View>

          <AuthFooter
            text="Keni një llogari? "
            linkText="Kyçu."
            onPress={goToLogin}
          />
        </AuthLayout>
      </View>
    </TouchableWithoutFeedback>
  );
}

const styles = StyleSheet.create({
  headerContainer: {
    marginTop: -10,
  },

  formContainer: {
    marginTop: 8,
    gap: 6,
  },

  buttonContainer: {
    marginTop: 30,
    alignItems: "center",
  },
});