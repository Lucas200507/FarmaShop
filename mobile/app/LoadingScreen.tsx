import React, { useEffect } from "react";
import { View, Image, StyleSheet } from "react-native";
import { useRouter } from "expo-router";

export default function LoadingScreen() {
    const router = useRouter();
    
    useEffect(() => {
        const timer = setTimeout(() => {
            router.replace("/CadastrarScreen");
        }, 3000); // 3 segundos

        return () => clearTimeout(timer);
    }, [router]);
    
    return (
        <View style={styles.container}>
            <Image
                source={require("@/assets/images/FarmaShopLogo.png")}
                style={styles.image}
            />
        </View>
    );
}

const styles = StyleSheet.create({
  container: {
    flex: 1, 
    backgroundColor: "#000038",
    justifyContent: "center", 
    alignItems: "center",
  },
  image: {
    width: 232,
    height: 164,
    resizeMode: "contain", 
  },
});
