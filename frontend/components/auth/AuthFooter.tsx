import React from "react";
import { Pressable, StyleSheet, Text, View } from "react-native";
import colors from "../../constants/colors";
import fonts from "../../constants/fonts";

type Props = {
  text: string;
  linkText: string;
  onPress: () => void;
};

export default function AuthFooter({ text, linkText, onPress }: Props) {
  return (
    <View style={styles.container}>
      <View style={styles.row}>
        <Text style={styles.text}>{text}</Text>
        <Pressable onPress={onPress}>
          <Text style={styles.link}>{linkText}</Text>
        </Pressable>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    marginTop: 22,
    alignItems: "center",
    paddingHorizontal: 25,
  },
  row: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    flexWrap: "wrap",
  },
  text: {
    color: colors.white,
    fontSize: 16,
    textAlign: "center",
    lineHeight: 22,
    fontWeight: "400",
    fontFamily: fonts.interRegular,
  },
  link: {
    color: colors.white,
    textDecorationLine: "underline",
    textDecorationColor: colors.white,
    fontStyle: "italic",
    transform: [{ skewX: "-12deg" }],
  },
});
