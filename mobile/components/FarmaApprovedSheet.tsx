import React from "react";
import { View, Text, StyleSheet } from "react-native";
import { Ionicons } from "@expo/vector-icons";

type FarmaApprovedSheetProps = {
  title: string;
  type?: "error" | "success"; // prop opcional
};

export default function FarmaApprovedSheet({ title, type = "error" }: FarmaApprovedSheetProps) {

    const isError = type === "error";
    const mainColor = isError ? "#830606" : "#5AB21F";
    
    return (
        <View style={[styles.header, {backgroundColor: mainColor}]}>
            <Ionicons 
                name= { isError ? "close" : "checkmark"} 
                size={64} 
                color="#ffffff" 
                style={[styles.icon, {backgroundColor: mainColor}]}
            />

            <Text style={styles.title}>{title}</Text>
        </View>
  );
}

const styles = StyleSheet.create({
  header: {
    width: "100%",
    paddingHorizontal: 20,
    paddingTop: 48, // espaço pra não colidir com o ícone
    paddingBottom: 60,
    borderTopLeftRadius: 60,
    borderTopRightRadius: 60,
    alignItems: "center",
    justifyContent: "center",
    position: "relative",
    elevation: 3,
    shadowColor: "#000",
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.08,
    shadowRadius: 6,
  },

  icon: {
    position: "absolute",
    top: -48, // metade do tamanho do ícone, pra ele “sair” um pouco do topo
    alignSelf: "center",
    borderRadius: 32,
    padding: 10,
    zIndex: 10,
    shadowColor: "#000",
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 6,
    elevation: 8, // Android
  },

  title: {
    fontSize: 24,
    fontWeight: "500",
    color: "#FFFFFF",
    textAlign: "center",
  },
});
