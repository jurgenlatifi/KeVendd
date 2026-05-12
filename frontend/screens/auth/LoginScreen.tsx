import { router } from "expo-router";
import React, { useState } from "react";
import {
  Keyboard,
  StyleSheet,
  Text,
  TouchableOpacity,
  TouchableWithoutFeedback,
  View,
} from "react-native";

import AuthButton from "@/components/auth/AuthButton";
import AuthFooter from "@/components/auth/AuthFooter";
import AuthHeader from "@/components/auth/AuthHeader";
import AuthLayout from "@/components/auth/AuthLayout";
import CustomInput from "@/components/auth/CustomInput";
import PasswordInput from "@/components/auth/PasswordInput";
import colors from "@/constants/colors";

export default function LoginScreen() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [secureText, setSecureText] = useState(true);
  const [error, setError] = useState("");

  const handleLogin = () => {
    setError("");

    if (!email || !password) {
      setError("Ju lutem plotësoni të gjitha fushat.");
      return;
    }

    router.replace("/(tabs)/map");
  };

  return (
    <TouchableWithoutFeedback onPress={Keyboard.dismiss} accessible={false}>
      <View style={{ flex: 1 }}>
        <AuthLayout>
          <AuthHeader
            title="Mirë se vini"
            subtitle="Rezervoni vendin tuaj të parkimit"
          />

          <View style={styles.formContainer}>
            <CustomInput
              value={email}
              onChangeText={setEmail}
              placeholder="E-mail"
              icon="email"
            />

            <PasswordInput
              value={password}
              onChangeText={setPassword}
              placeholder="Fjalëkalimi"
              secureTextEntry={secureText}
              onToggleSecure={() => setSecureText(!secureText)}
            />
          </View>

          <View style={styles.optionsRow}>
            <View />
            <TouchableOpacity
              onPress={() => router.push("/(auth)/forgot-password")}
            >
              <Text style={styles.forgotText}>Keni harruar fjalëkalimin?</Text>
            </TouchableOpacity>
          </View>

          {error ? <Text style={styles.errorText}>{error}</Text> : null}

          <View style={styles.buttonContainer}>
            <AuthButton title="Kyçu" onPress={handleLogin} />
          </View>

          <AuthFooter
            text="Nuk keni një llogari? "
            linkText="Regjistrohuni."
            onPress={() => router.replace("/(auth)/register")}
          />
        </AuthLayout>
      </View>
    </TouchableWithoutFeedback>
  );
}

const styles = StyleSheet.create({
  formContainer: {
    marginTop: 26,
  },
  optionsRow: {
    marginTop: 8,
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
  },
  forgotText: {
    color: colors.white,
    fontSize: 14,
    fontStyle: "italic",
    textDecorationLine: "underline",
    textDecorationColor: colors.white,
    transform: [{ skewX: "-12deg" }],
  },
  buttonContainer: {
    marginTop: 10,
    alignItems: "center",
  },
  errorText: {
    color: "red",
    textAlign: "center",
    marginTop: 10,
    fontSize: 13,
  },
});