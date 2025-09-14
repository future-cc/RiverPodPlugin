package com.example.riverpod_page_plugin

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import java.io.IOException

class CreateSimplePageAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.getProject()
        val dir = e.getData<VirtualFile?>(PlatformDataKeys.VIRTUAL_FILE)
        if (project == null || dir == null) return

        // 弹出输入框
        val fileName = Messages.showInputDialog(
            project,
            "Enter file name (e.g. home):", "Riverpod Page Generator",
            Messages.getQuestionIcon()
        )
        if (fileName == null || fileName.isEmpty()) return

        val pageName = "${fileName}_page"

        // 把所有写操作包到 runWriteAction 里
        ApplicationManager.getApplication().runWriteAction(Runnable {
            try {
                // 创建文件夹
                val folder: VirtualFile = dir.createChildDirectory(this, fileName)

                // 大驼峰类名
                val className = toPascalCase(pageName)

                // page.dart
                val pageContent =
                    """
                        import '${pageName}_controller.dart';
                        import 'package:flutter/material.dart';
                        import 'package:flutter_riverpod/flutter_riverpod.dart';
                        
                        class $className extends ConsumerWidget {
                          const $className({super.key});
                        
                            Widget _buildView(BuildContext context) {
                              return Text("$pageName");
                            }

                            @override
                            Widget build(BuildContext context, WidgetRef ref) {
                              var counter = ref.watch(mainPageControllerProvider);
                              return Scaffold(
                                appBar: AppBar(title: Text('appbarTitle')),
                                body: _buildView(context),
                              );
                            }
                        }
                    """.trimIndent()
                createDartFile(folder,  "${pageName}.dart", pageContent)


                // controller.dart
                val controllerContent =
                    """
                        import 'package:flutter/cupertino.dart';
                        import 'package:riverpod_annotation/riverpod_annotation.dart';
                        part '${pageName}_controller.g.dart';
                        
                        @riverpod
                        class ${className}Controller extends _$${className}Controller {
                        
                          @override
                          int build() {
                            // 初始状态
                            ref.onDispose(() => dispose());
                            return 1;
                          }
                        
                          void updateData() {
                            state++;
                          }
                        
                          void dispose() {
                          }
                        } 
                    """.trimIndent()
                createDartFile(folder,  "${pageName}_controller.dart", controllerContent)


                // index.dart
                val indexContent =
                    """
                        import '${pageName}.dart';
                        import '${pageName}_controller.dart';
                    """.trimIndent()
                createDartFile(folder,  "index.dart", indexContent)
            } catch (ex: IOException) {
                Messages.showErrorDialog(project, "Failed to create files: " + ex.message, "Error")
            }
        })
    }

    private fun createDartFile(folder: VirtualFile, pageName: String, dartContent: String) {
        val controllerFile = folder.createChildData(this, pageName)
        VfsUtil.saveText(controllerFile, dartContent)
    }

    private fun toPascalCase(name: String): String {
        val parts = name.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val sb = StringBuilder()
        for (part in parts) {
            if (part.isEmpty()) continue
            sb.append(part.get(0).uppercaseChar())
                .append(part.substring(1))
        }
        return sb.toString()
    }
}