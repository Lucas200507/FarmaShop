import React from "react";
import { Text, StyleSheet, TouchableOpacity } from "react-native";
export default function FarmaTagButton({label, action}: {label: string, action: () => void}) {
    return(
        <TouchableOpacity style={styles.container} onPress={action}>
            <Text style={styles.text}>{ label }</Text>
        </TouchableOpacity>
    );
}
    
const styles = StyleSheet.create({
    container: {
        backgroundColor: '#D9D9D9',
        borderRadius: 10,
        width: 100,
        height: 30,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
    },
    text: {
        fontWeight: '500',
        
    }
});