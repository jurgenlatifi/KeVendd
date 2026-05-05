import { LinearGradient } from "expo-linear-gradient";
import React from "react";
import { StyleSheet, Text, TouchableOpacity } from "react-native";
import colors from "../../constants/colors";
import fonts from "../../constants/fonts";

type Props = {
  title: string;
  onPress: () => void;
};

export default function AuthButton({ title, onPress }: Props) {
  return (
    <TouchableOpacity
      onPress={onPress}
      activeOpacity={0.85}
      style={styles.wrapper}
    >
      <LinearGradient
        colors={[
          colors.primaryBlue,
          colors.primaryDarkBlue,
          colors.primaryDarkBlue,
          colors.primaryBlue,
        ]}
        start={{ x: 0, y: 0 }}
        end={{ x: 1, y: 0 }}
        style={styles.button}
      >
        <Text style={styles.text}>{title}</Text>
      </LinearGradient>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  wrapper: {
    alignSelf: "center",
    width: 255,
    marginTop: 28,
  },
  button: {
    width: "100%",
    height: 52,
    borderRadius: 25,
    justifyContent: "center",
    alignItems: "center",
  },
  text: {
    color: colors.white,
    fontSize: 20,
    fontFamily: fonts.interSemiBold,
    fontWeight: "600",
    textAlign: "center",
  },
});