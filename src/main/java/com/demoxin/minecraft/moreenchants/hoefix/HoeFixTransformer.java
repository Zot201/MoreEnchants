package com.demoxin.minecraft.moreenchants.hoefix;

import java.util.Iterator;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class HoeFixTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals("net.minecraft.item.ItemHoe")) {
            return patchClass(name, basicClass, Launch.blackboard.get("fml.deobfuscatedEnvironment").equals(false));
        }

        return basicClass;
    }
    
    private byte[] patchClass(String name, byte[] bytes, boolean obfuscated)
    {
        System.out.println("Now Patching ItemHoe...");
        if(obfuscated)
            System.out.println("Obfuscated Environment Detected!");
        
        String methodGetItemEnchantability;
        String methodGetEnchantability;
        String fieldToolMaterial;
        if(obfuscated == true)
        {
            methodGetItemEnchantability = "func_77619_b";
            methodGetEnchantability = "func_77995_e";
            fieldToolMaterial = "field_77843_a";
        }
        else
        {
            methodGetItemEnchantability = "getItemEnchantability";
            methodGetEnchantability = "getEnchantability";
            fieldToolMaterial = "theToolMaterial";
        }
        
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);
        
        MethodNode newMethod = new MethodNode(Opcodes.ACC_PUBLIC, methodGetItemEnchantability, "()I", null, null);
        newMethod.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/item/ItemHoe", fieldToolMaterial, Type.getObjectType("net/minecraft/item/Item$ToolMaterial").toString()));
        newMethod.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/item/Item$ToolMaterial", methodGetEnchantability, "()I"));
        newMethod.instructions.add(new InsnNode(Opcodes.IRETURN));
        classNode.methods.add(newMethod);

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        
        System.out.println("Patching Complete!");
        
        return writer.toByteArray();
    }
}
