import { Ionicons, MaterialCommunityIcons } from "@expo/vector-icons";
import { LinearGradient } from "expo-linear-gradient";
import { router } from "expo-router";
import React from "react";
import { Pressable, StyleSheet } from "react-native";

const tabs = [
  { name: "map", route: "/map", icon: "map-outline", family: "material" },
  { name: "list", route: "/reservation-history", icon: "list-outline", family: "ion" },
  { name: "home", route: "/home", icon: "home-outline", family: "ion" },
  { name: "account", route: "/account", icon: "person-outline", family: "ion" },
];

type Props = {
  activeTab: "map" | "list" | "home" | "account";
};

export default function BottomTabBar({ activeTab }: Props) {
  return (
    <LinearGradient
      colors={["#3080FF", "#00358B", "#00358B", "#3080FF"]}
      locations={[0, 0.378, 0.6298, 1]}
      start={{ x: 0, y: 0 }}
      end={{ x: 1, y: 0 }}
      style={styles.container}
    >
      {tabs.map((tab) => {
        const isActive = activeTab === tab.name;

        return (
          <Pressable
            key={tab.name}
            onPress={() => router.push(tab.route as any)}
            style={[styles.tab, isActive && styles.activeTab]}
          >
            {tab.family === "material" ? (
              <MaterialCommunityIcons
                name={tab.icon as any}
                size={26}
                color="#FFFFFF"
              />
            ) : (
              <Ionicons name={tab.icon as any} size={26} color="#FFFFFF" />
            )}
          </Pressable>
        );
      })}
    </LinearGradient>
  );
}

const styles = StyleSheet.create({
  container: {
    position: "absolute",
    bottom: 17,
    left: 40,
    right: 40,
    height: 61,
    borderRadius: 30,
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-around",
    shadowColor: "#000",
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.25,
    shadowRadius: 4,
    elevation: 8,
  },

  tab: {
    width: 69,
    height: 43,
    borderRadius: 50,
    alignItems: "center",
    justifyContent: "center",
  },

  activeTab: {
    backgroundColor: "rgba(255,255,255,0.36)",
  },
});