package com.dmiesoft.fitpomodoro.utils;

import java.util.HashMap;
import java.util.TreeMap;

public class LicensesDataProvider {

    private HashMap<String, String> map = new HashMap<>();

    {
        addLicense("EventBus License",
                "EventBus binaries and source code can be used according to the Apache License, Version 2.0:\n" +
                        "\n" +
                        "Copyright © 2012-2016 Markus Junginger, greenrobot (http://greenrobot.org)\n" +
                        "\n" +
                        "Licensed under the Apache License, Version 2.0 (the “License”); you may not use this file except in compliance with the License. You may obtain a copy of the License at\n" +
                        "\n" +
                        "http://www.apache.org/licenses/LICENSE-2.0\n" +
                        "\n" +
                        "Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License."
        );

        addLicense("MaterialSeekBarPreference \n [ https://git.io/vyz8H ]",
                "Lib is licenced under Apache2 licence, so you can do whatever you want with it. I'd highly recommend to push changes back to make it cooler :D"
        );

        addLicense("TextDrawable \n [https://git.io/vyz86]",
                "The MIT License (MIT)\n" +
                        "\n" +
                        "Copyright (c) 2014 Amulya Khare\n" +
                        "\n" +
                        "Permission is hereby granted, free of charge, to any person obtaining a copy\n" +
                        "of this software and associated documentation files (the \"Software\"), to deal\n" +
                        "in the Software without restriction, including without limitation the rights\n" +
                        "to use, copy, modify, merge, publish, distribute, sublicense, and/or sell\n" +
                        "copies of the Software, and to permit persons to whom the Software is\n" +
                        "furnished to do so, subject to the following conditions:\n" +
                        "\n" +
                        "The above copyright notice and this permission notice shall be included in all\n" +
                        "copies or substantial portions of the Software.\n" +
                        "\n" +
                        "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR\n" +
                        "IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,\n" +
                        "FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE\n" +
                        "AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER\n" +
                        "LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,\n" +
                        "OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE\n" +
                        "SOFTWARE."
        );

//
//        // Main License
//        addLicense("FitPomodoro v1.0.0 [ beta ]",
//                "© 2017 DMieSoft\n Third-party licenses:"
//        );

    }

    private void addLicense(String header, String license) {
        map.put(header, license);
    }

    public HashMap<String, String> getLicenses() {
        return map;
    }

}
