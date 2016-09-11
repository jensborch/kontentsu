import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpModule } from '@angular/http';
import { MdToolbarModule } from '@angular2-material/toolbar';

import { AppComponent }   from './app.component';

@NgModule({
    imports: [BrowserModule, HttpModule, MdToolbarModule],
    declarations: [AppComponent],
    bootstrap: [AppComponent]
})
export class AppModule { }