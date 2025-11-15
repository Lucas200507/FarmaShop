import React from "react";
import { View, StyleSheet, Text, Image, TouchableOpacity } from "react-native";
import { LinearGradient } from 'expo-linear-gradient';

export default function FarmaPromoCard({name, price, action}: {name: string; price: string, action: () => void}) {
  return (
    <TouchableOpacity onPress={action}>
    <LinearGradient
      colors={['#6AD7BF', '#000038']} // suas cores
      start={{ x: 0, y: 0 }}
      end={{ x: 1, y: 1 }}
      style={styles.container}
    >
        <Image
            style={{ width: 94, height: 94 }}
            source={require('@/assets/images/FarmaMedicamento.png')}
        />
        <View>
        <Text style={styles.text}>{ name }</Text>
        <View style={{ flexDirection: 'row'}}>
        <View style={{ flexDirection: 'column'}}>
            <Text style={[styles.text, {fontSize: 16}]}>por</Text>
            <Text style={styles.text}>R$</Text>
        </View>
        
        <Text style={[styles.text, {fontSize: 48}]}>{ price }</Text>
        </View>
        </View>
    </LinearGradient>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
    container: {
        width: 300,
        height: 124,
        borderRadius: 10,
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-around',
    },
    text: {
        color: '#ffffff',
        fontWeight: '500',
        fontSize: 25,
    }
})