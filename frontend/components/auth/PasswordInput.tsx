import { MaterialIcons } from "@expo/vector-icons";
import React from "react";
import { StyleSheet, TextInput, TouchableOpacity, View } from "react-native";
import colors from "../../constants/colors";
import fonts from "../../constants/fonts";

type Props = {
  value: string;
  onChangeText: (text: string) => void;
  placeholder: string;
  secureTextEntry: boolean;
  onToggleSecure: () => void;
};

export default function PasswordInput({
  value,
  onChangeText,
  placeholder,
  secureTextEntry,
  onToggleSecure,
}: Props) {
  return (
    <View style={styles.container}>
      <MaterialIcons name="lock" size={20} color={colors.white} style={styles.leftIcon} />

      <TextInput
        value={value}
        onChangeText={onChangeText}
        placeholder={placeholder}
        placeholderTextColor="rgba(255,255,255,0.75)"
        secureTextEntry={secureTextEntry}
        style={styles.input}
      />

      <TouchableOpacity onPress={onToggleSecure} style={styles.eyeButton} activeOpacity={0.7}>
        <MaterialIcons
          name={secureTextEntry ? "visibility-off" : "visibility"}
          size={22}
          color="rgba(255,255,255,0.8)"
        />
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    width: "100%",
    height: 55,
    backgroundColor: "rgba(199, 221, 255, 0.4)",
    borderRadius: 12,
    flexDirection: "row",
    alignItems: "center",
    paddingHorizontal: 16,
    marginBottom: 12,
  },
  leftIcon: {
    marginRight: 12,
  },
  input: {
    flex: 1,
    color: colors.white,
    fontSize: 15,
    fontWeight: "500",
    fontFamily: fonts.interRegular,
    letterSpacing: 1,
    paddingVertical: 0,
    paddingRight: 12,
  },
  eyeButton: {
    justifyContent: "center",
    alignItems: "center",
    paddingLeft: 8,
  },
});