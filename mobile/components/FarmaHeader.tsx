import React from "react";
import { View, Text, Image, StyleSheet } from "react-native";

export default function FarmaHeader({ title }: { title: string }) {
  return (
    <View style={styles.header}>
      <Text style={styles.title}>{title}</Text>
      <Image
        style={styles.logo}
        source={require("@/assets/images/FarmaShopIcon.png")}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  header: {
    width: "100%",
    backgroundColor: "#fff",
    paddingHorizontal: 20,
    paddingTop: 20,
    paddingBottom: 60,
    marginBottom: 60,
    borderBottomLeftRadius: 60,
    borderBottomRightRadius: 60,
    alignItems: "center",
    justifyContent: "center",
    position: "relative", 
    elevation: 3,
    shadowColor: "#000",
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.08,
    shadowRadius: 6,
  },
  logo: {
    width: 96,
    height: 96,
    position: "absolute",
    bottom: -48,
    alignSelf: "center",
    backgroundColor: "#000038",
    borderRadius: 48,
    padding: 10,
    zIndex: 10,
  },
  title: {
    fontSize: 24,
    fontWeight: "500",
    color: "#000038",
    textAlign: "center",
  },
});
