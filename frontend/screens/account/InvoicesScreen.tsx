import React from "react";
import { Image, SafeAreaView, StyleSheet, Text, View } from "react-native";

import ScreenHeader from "../../components/common/ScreenHeader";
import BottomTabBar from "../../components/navigation/BottomTabBar";
import BackButton from "../../components/common/BackButton";
import fonts from "../../constants/fonts";

export default function InvoicesScreen() {
  return (
    <SafeAreaView style={styles.container}>
      
      {/* BACK BUTTON */}
      <BackButton top={130} left={18} />

      {/* HEADER */}
      <ScreenHeader />

      {/* TITLE */}
      <Text style={styles.title}>Faturat e Mia</Text>

      {/* EMPTY STATE */}
      <View style={styles.emptyContainer}>
        <Image
          source={require("../../assets/invoice.png")}
          style={styles.emptyImage}
          resizeMode="contain"
        />

        <Text style={styles.emptyText}>
          Ju nuk keni asnjë faturë për momentin.
        </Text>
      </View>

      {/* BOTTOM TAB */}
      <BottomTabBar activeTab="account" />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#000000",
  },

  title: {
    marginTop: 30,
    marginLeft: 20,
    color: "#FFFFFF",
    fontSize: 29,
    letterSpacing: -1,
    fontFamily: fonts.interRegular,
  },

  emptyContainer: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
    paddingBottom: 100,
  },

  emptyImage: {
    width: 143,
    height: 126,
    marginBottom: 20,
  },

  emptyText: {
    color: "#FFFFFF",
    fontSize: 14,
    textAlign: "center",
    fontFamily: fonts.interRegular,
  },
});