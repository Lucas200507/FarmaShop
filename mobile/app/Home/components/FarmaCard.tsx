import React from "react";

import { Text, Image, StyleSheet, View, TouchableOpacity } from "react-native";

export default function FarmaCard({label, price, action}: {label: string; price: string, action: () => void}) {
    return(
        <TouchableOpacity onPress={action}>
        <View style={styles.container}>
            <Image 
                source={require('@/assets/images/FarmaMedicamento.png')}
                style={styles.image}
            />
            <Text style={styles.text}>{ label }</Text>
            <Text style={styles.text}>R$ { price }</Text>
        </View>
        </TouchableOpacity>
    );
}

const styles = StyleSheet.create({
    container: {
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        width: 160,
        height: 160,
        backgroundColor: '#D9D9D9',
        borderRadius: 16,
        
    },
    image: {
        width: 97,
        height: 97,
    },
    text: {
        fontWeight: 400,
        fontSize: 25
    }
});