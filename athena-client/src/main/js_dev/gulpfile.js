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
    return gulp.src('scripts/**')
//        .pipe(uglify())
        .pipe(gulp.dest('../webapp/resources/app/js'));
});

gulp.task('libs', function(){
    var marionette = gulp.src('node_modules/backbone.marionette/lib/backbone.marionette.js');
    var underscore = gulp.src('node_modules/backbone.marionette/node_modules/underscore/underscore.js');
    var jquery = gulp.src('node_modules/jquery/dist/jquery.js');
    var bootstrap = gulp.src('bower_components/bootstrap-sass/assets/javascripts/bootstrap.js');
    var backbone = gulp.src('node_modules/backbone.marionette/node_modules/backbone/backbone.js');
    var dataTables = gulp.src('data_tables/jquery.dataTables.js');
    var dataTablesBS = gulp.src('data_tables/dataTables.bootstrap.js');
    var dataTablesScroller = gulp.src('data_tables/dataTables.scroller.js');
    var spinnerJquery = gulp.src('spinner/jquery.spin.js');
    var spinner = gulp.src('spinner/spin.js');

    return es.merge(marionette, underscore, jquery, bootstrap,
        backbone, dataTables,dataTablesBS, dataTablesScroller, spinnerJquery, spinner)
        .pipe(gulp.dest('../webapp/resources/app/lib'))
});

gulp.task('sass', function(){
    return gulp.src('./scss/main.scss')
        .pipe(rename({suffix: '.min'}))
        .pipe(sass({style: 'compressed'}))
        .pipe(gulp.dest('../webapp/resources/app/css'));
});

gulp.task('css', function(){
    return gulp.src('./scss/*.css')
        .pipe(gulp.dest('../webapp/resources/app/css'));
});

gulp.task('fonts', function(){
    return gulp.src('./fonts/**')
        .pipe(gulp.dest('../webapp/resources/app/fonts'));
});

gulp.task('images', function(){
    return gulp.src('./images/**')
        .pipe(gulp.dest('../webapp/resources/app/images'));
});

gulp.task('templates', function(){
    return gulp.src('./templates/**')
        .pipe(gulp.dest('../webapp/resources/app/templates'));
});
gulp.task('default', function(){
    return del(['../webapp/resources/app/*'],{force: true}, function(){
        return rs('scripts', 'libs', 'css', 'sass', 'images', 'templates', 'fonts');
    });
});

//gulp.task('default', function(){
//    return rs('clean', ['scripts', 'libs', 'css', 'sass', 'images']);
//});
