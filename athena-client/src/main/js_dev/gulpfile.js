/**
 * Created by GMalikov on 24.04.2015.
 */
var gulp = require('gulp');
var uglify = require('gulp-uglify');
var es = require('event-stream');
var rs = require('run-sequence');
var sass = require('gulp-sass');
var rename = require('gulp-rename');
var del = require('del');


gulp.task('scripts', function(){
    return gulp.src('scripts/*.js')
        .pipe(uglify())
        .pipe(gulp.dest('../webapp/resources/app/js'));
});

gulp.task('libs', function(){
    var marionette = gulp.src('node_modules/backbone.marionette/lib/backbone.marionette.js');
    var underscore = gulp.src('node_modules/backbone.marionette/node_modules/underscore/underscore.js');
    var jquery = gulp.src('node_modules/jquery/dist/jquery.js');
    var bootstrap = gulp.src('bower_components/bootstrap-sass/assets/javascripts/bootstrap.js')

    return es.merge(marionette, underscore, jquery, bootstrap)
        .pipe(gulp.dest('../webapp/resources/app/lib'))
});

gulp.task('sass', function(){
    return gulp.src('./scss/main.scss')
        .pipe(rename({suffix: '.min'}))
        .pipe(sass({style: 'compressed'}))
        .pipe(gulp.dest('../webapp/resources/app/css'));
});

gulp.task('clean', function(){
    return del(['../webapp/resources/app/*'],{force: true}, null);
});

gulp.task('default', function(){
    return rs('clean', 'scripts', 'libs', 'sass');
});
