import { MaterialIcons } from "@expo/vector-icons";
import { router } from "expo-router";
import React, { useState } from "react";
import {
  Keyboard,
  Pressable,
  StyleSheet,
  Text,
  TouchableOpacity,
  TouchableWithoutFeedback,
  View,
} from "react-native";

import AuthButton from "../../components/auth/AuthButton";
import AuthFooter from "../../components/auth/AuthFooter";
import AuthHeader from "../../components/auth/AuthHeader";
import AuthLayout from "../../components/auth/AuthLayout";
import CustomInput from "../../components/auth/CustomInput";
import PasswordInput from "../../components/auth/PasswordInput";
import colors from "../../constants/colors";
import fonts from "../../constants/fonts";

export default function LoginScreen() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [rememberMe, setRememberMe] = useState(false);
  const [secureText, setSecureText] = useState(true);

  const handleLogin = () => {
    console.log("Login pressed");
  };

  return (
    <TouchableWithoutFeedback onPress={Keyboard.dismiss} accessible={false}>
      <View style={{ flex: 1 }}>
        <AuthLayout>
          <AuthHeader
            title="Mirë se vini"
            subtitle="lorem ipsum............................"
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
            <Pressable
              style={styles.rememberContainer}
              onPress={() => setRememberMe(!rememberMe)}
            >
              <View style={[styles.checkbox, rememberMe && styles.checkboxChecked]}>
                {rememberMe ? (
                  <MaterialIcons
                    name="check"
                    size={10}
                    color={colors.checkboxBackground}
                  />
                ) : null}
              </View>

              <Text style={styles.rememberText}>Më mbaj mend</Text>
            </Pressable>

            <TouchableOpacity
              onPress={() => router.push("/forgot-password" as any)}
            >
              <Text style={styles.forgotText}>Ke harruar fjalekalimin?</Text>
            </TouchableOpacity>
          </View>

          <View style={styles.buttonContainer}>
            <AuthButton title="Kyçu" onPress={handleLogin} />
          </View>

          {/* JUST TO SEE THE PAGE KUJTOHU QE TA HEQESH */}
          <View style={styles.testButtonContainer}>
            <TouchableOpacity
              onPress={() => router.push("/change-password" as any)}
            >
              <Text style={styles.testButtonText}>Go to Change Password</Text>
            </TouchableOpacity>
          </View>

          {/* JUST TO SEE THE Account KUJTOHU QE TA HEQESH */}
          <View style={styles.testButtonContainer}>
            <TouchableOpacity onPress={() => router.push("/account" as any)}>
              <Text style={styles.testButtonText}>Go to Account</Text>
            </TouchableOpacity>
          </View>

          <AuthFooter
            text="Nuk keni një llogari? "
            linkText="Regjistrohu."
            onPress={() => router.push("/register" as any)}
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
  rememberContainer: {
    flexDirection: "row",
    alignItems: "center",
  },
  checkbox: {
    width: 11,
    height: 11,
    borderWidth: 1,
    borderColor: colors.white,
    backgroundColor: "rgba(31, 78, 153, 0.25)",
    borderRadius: 1,
    marginRight: 8,
    alignItems: "center",
    justifyContent: "center",
  },
  checkboxChecked: {
    backgroundColor: colors.white,
  },
  rememberText: {
    color: colors.white,
    fontSize: 14,
    fontFamily: fonts.interRegular,
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
    marginTop: 56,
    alignItems: "center",
  },
  //KUJTOHU TI HEQESH
  testButtonContainer: {
    marginTop: 10,
    alignItems: "center",
  },

  testButtonText: {
    color: "rgba(255,255,255,0.6)",
    fontSize: 12,
    textDecorationLine: "underline",
  },
});