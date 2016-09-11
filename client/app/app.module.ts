import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { MdToolbarModule } from '@angular2-material/toolbar';

import { AppComponent }   from './app.component';

@NgModule({
    imports: [BrowserModule, MdToolbarModule],
    declarations: [AppComponent],
    bootstrap: [AppComponent]
})
export class AppModule { }