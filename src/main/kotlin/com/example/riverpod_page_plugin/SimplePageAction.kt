package com.example.riverpod_page_plugin

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CreateSimplePageAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.getProject()
        val dir = e.getData<VirtualFile?>(PlatformDataKeys.VIRTUAL_FILE)
        if (project == null || dir == null) return

        // 弹出输入框
        val (folderName, description) = FolderNameInputDialog.showTwoInputDialog()
        if (folderName != null) {
            println("Folder: $folderName")
            println("Description: $description")
        }

        if (folderName == null || folderName.isEmpty()) return

        // 把所有写操作包到 runWriteAction 里
        ApplicationManager.getApplication().runWriteAction(Runnable {
            try {
                val currentDate: String = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
                // 创建文件夹
                val folder: VirtualFile = dir.createChildDirectory(this, folderName)

                val pageName = "${folderName}_page"
                // 大驼峰类名
                val classNamePrefix = toPascalCase(pageName)

                val providerPrefix = classNamePrefix.take(1).lowercase() + classNamePrefix.substring(1)
                // game_setting_page.dart
                val pageCode =
                    """
                        import 'package:flutter/material.dart';
                        import 'package:flutter_riverpod/flutter_riverpod.dart';
                        import '${folderName}_notifier.dart';
                        import '${folderName}_state.dart';
                        
                        /// $description page
                        ///
                        /// kane $currentDate
                        class ${classNamePrefix}Page extends ConsumerStatefulWidget {
                          const ${classNamePrefix}Page({super.key});
                          @override
                          ConsumerState<${classNamePrefix}Page> createState() => _${classNamePrefix}PageState();
                        }
    
                        class _${classNamePrefix}PageState extends ConsumerState<${classNamePrefix}Page> {
                          @override
                          void initState() {
                            super.initState();
                          }
    
                          @override
                          Widget build(BuildContext context) {                                   
                            final state = ref.watch(${providerPrefix}NotifierProvider);
                            final notifier = ref.read(${providerPrefix}NotifierProvider.notifier);
    
                            return Scaffold(
                              body: Container(
                                color: Colors.white,
                                child: _buildView(state, notifier),
                              ),
                            );
                          }
    
                          Widget _buildView(${classNamePrefix}State state, ${classNamePrefix}Notifier notifier) {
                            return Text("${description}页面");
                          }
                        }
                    """.trimIndent()
                createDartFile(folder,  "${folderName}_page.dart", pageCode)

                // game_setting_notifier.dart
                val notifierCode =
                    """
                        import 'package:riverpod_annotation/riverpod_annotation.dart';
                        import '${folderName}_state.dart';
                        part '${folderName}_notifier.g.dart';
    
                        /// $description Notifier
                        ///
                        /// Kane $currentDate
                        @riverpod
                        class ${classNamePrefix}Notifier extends _$${classNamePrefix}Notifier {
    
                          @override
                          ${classNamePrefix}State build() {
                            // 初始状态
                            return ${classNamePrefix}State.initial();
                          }
                        }
                    """.trimIndent()

                createDartFile(folder,  "${folderName}_notifier.dart", notifierCode)


                // game_detail_state.dart
                val stateCode = """
                        import 'package:freezed_annotation/freezed_annotation.dart';
                        part '${folderName}_state.freezed.dart';
    
                        /// $description state
                        ///
                        /// kane ${currentDate}
                        @freezed
                        abstract class ${classNamePrefix}State with _$${classNamePrefix}State {    
                          const factory ${classNamePrefix}State({
                              @Default(0) int defaultValue,
                          }) = _${classNamePrefix}State;
    
                          /// 初始状态
                          factory ${classNamePrefix}State.initial() {
                            return const ${classNamePrefix}State();
                          }
                        }
                    """.trimIndent()
                createDartFile(folder,  "${folderName}_state.dart", stateCode)
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