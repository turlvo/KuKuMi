'use strict';
module.exports = function(grunt) {

	grunt.initConfig({
		uglify: {
			options: {
			},
			taskone: {
				src: './src/jquery.loading.js',
				dest: './dist/jquery.loading.min.js'
			},
			tasktwo: {
				src: './src/loading.js',
				dest: './dist/loading.min.js'
			}
		},
		cssmin: {
			//文件头部输出信息
			options: {
				//美化代码
				beautify: {
					//中文ascii化，非常有用！防止中文乱码的神配置
					ascii_only: true
				}
			},
			basic: {
				src: ['./src/loading.css'],
				dest: './dist/loading.min.css'
			}
		},
		postcss: {
			options: {
				processors: [
					require('autoprefixer')()
				]
			},
			dist: {
				files: [{
					src: './dist/loading.min.css',
					dest: './dist/loading.min.css'
				}]
			}
		}
	});

	grunt.loadNpmTasks('grunt-contrib-uglify');
	grunt.loadNpmTasks('grunt-postcss');
	grunt.loadNpmTasks('grunt-contrib-cssmin');

	grunt.registerTask('default', ['uglify', 'cssmin', 'postcss']);

};