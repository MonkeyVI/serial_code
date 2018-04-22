package com.nowpay.wio.test;

import com.nowpay.common.util.URLUtils;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.buffer.DynamicChannelBuffer;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.jboss.netty.handler.codec.http.*;
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.*;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * HttpsNetty处理器
 * User: 韩彦伟
 * Date: 14-8-6
 * Time: 上午11:11
 * To change this template use File | Settings | File Templates.
 */
public class HttpsNettyHandlerTest extends SimpleChannelUpstreamHandler {

    private Logger logger = LoggerFactory.getLogger(HttpsNettyHandlerTest.class);

    private final static String responseContent = "<html>\n" +
            "<head>\n" +
            "</head>\n" +
            "<body>\n" +
            "<form action=\"https://payment.chinapay.com/pay/TransGet\" METHOD=POST>\n" +
            "<input type=text name=\"MerId\" value=\"808080201305223\"/>\n" +
            "<input type=text name=\"OrdId\" value=\"0000000000000006\"/>\n" +
            "<input type=text name=\"TransAmt\" value=\"000000001234\"/>\n" +
            "<input type=text name=\"CuryId\" value=\"156\"/>\n" +
            "<input type=text name=\"TransDate\" value=\"20140828\"/>\n" +
            "<input type=text name=\"TransType\" value=\"0001\"/>\n" +
            "<input type=text name=\"Version\" value=\"20070129\"/>\n" +
            "<input type=text name=\"BgRetUrl\" value=\"http://www.baidu.com\"/>\n" +
            "<input type=text name=\"PageRetUrl\" value=\"www.baidu.com \"/>\n" +
            "<input type=text name=\"GateId\" value=\"1023\">\n" +
            "<input type=text name=\"Priv1\" value=\"\">\n" +
            "<input type=text name=\"ChkValue\" value=\"959BD7ED586E987204B25E227EB65F2B6724D6748385EC7429C50C59D0501C32233AB232CD73BCA94658F7AD9FCB70C616A5B43F646E4D4D47A40BEDAC8B1994D07F114963DD5E928ED66296E9153F497F4B67BF9A32F1C688A488C2A6E28AE14CBCA73B8787066522728624CD059B129869022742330867F7A32D967CAE8859\">\n" +
            "\n" +
            "<button type=submit>submit</button>\n" +
            "</form>\n" +
            "\n" +
            "\n" +
            "<img width=\"200\" height=\"100\" src=\"data:image/jpg;base64,/9j/4AAQSkZJRgABAgAAZABkAAD/7AARRHVja3kAAQAEAAAAZAAA/+4ADkFkb2JlAGTAAAAAAf/bAIQAAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQICAgICAgICAgICAwMDAwMDAwMDAwEBAQEBAQECAQECAgIBAgIDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMD/8AAEQgAQwEfAwERAAIRAQMRAf/EANAAAAICAwACAwAAAAAAAAAAAAAJCAoFBgcECwECAwEAAgICAwEBAAAAAAAAAAAAAAcFBgQIAgMJCgEQAAAHAAECAwILAg0ACgMAAAECAwQFBgcIAAkREhMhFCIVVRbWV5fXGJgaFxkxQVFhMiMk1LdYOXkKcUJScjMlNSZIiMhZqREAAgECBAMDBgoGBgcIAwAAAQIDEQQAEgUGIRMHMUEiUWGTFBYXcTJS0iPTVBVVCIGRQrN1N6HRcjMklGKCkqJWGAmyUzSExEWlJrHBlf/aAAwDAQACEQMRAD8Av8dGDC3+5H3AoXgjmcE/jINhctb0R1JR+eVSScOEIZBGISbHmrZaDMlEXx4KGUftkwbIKIuHzhwVNNRIhVlkaZvPd0W1bJHRRJqExIjQ9nCmZ2pxyioFAQWJoCBUjaP8rX5bb/8AMPuu4tru4ksdl6YiPe3CBTKTIWEVvBmBQTShHbOyskSIWZXYpG9aZ53tu4O5dOHCOlU+PRXWUVTYs8uox2rMhzCYrZud/EPnpkUQHylFVZVQQD4RjD7ekq3U3dzMSJ4wCewRJQeYVBP6yceqsH5B/wAtUUKxSaVfSuqgF2v7sMxH7TBJESp7TlVV8gA4Y8b99f3C/rWrH2WZ39HeuPvM3f8AaE9FH83Hb/yFfln/AAe7/wD6F79diPmq/wDIQ7itWkD1yuajTzyiKaR5CQc5TnaybEViAqk2bt/m8BFXIpHKcxjiYhAMAeUTCIl3n/LV0g17qRoi7939eSJtyWR1tbaKOONrgRsUeWSXKWSIOrIqIFd2UtzFQASeMH/UM390G/L7vKToj0Q2+k3UC2gik1HUbq9vZ4rBp4xLFbW9vz1jlujC8c0ksxeGFJFj5EkjM0HF/wBRB3Ufrtpv2LZT9FetufcJ0y+xzf5if5+PLT35dR/tcXoIfmYP1EHdR+u2m/YtlP0V6PcJ0y+xzf5if5+D35dR/tcXoIfmY26md/Put2+W9xS3Gmt2jcgOH7v9iuUmFFDzAUCpFGqgB11jD4FAfYHtEfYHh1C670d6W6JZ+sPYzNMxoi+sT8T5/HwA7/0DvxXdy/mS6gbdsPWnuonuHOWNORCKtStScnBQOJ/QO/EiEu+J3HE0yEPsFZXMUPAVVcmzIFFB/wC0cEauikA/90pQ6WT9ONoMxZbZlB7hLLQfrcn9ZOE/J+bTra7ll1C2VT3C0t6D4M0ZP6ycff8Afj9xr626t9k+b/Rvrj7ttpf9w/pZPnY4f82XW78St/8AKW31WD9+P3Gvrbq32T5v9G+j3bbS/wC4f0snzsH/ADZdbvxK3/ylt9VjaqV34efVetMJMWu1Ua/1xk/bLTVQks+qsA3nI0qpBeMSTdai46XinSzfzFRcJnUKioIHMkqUBTN0XPTLa8sDRwJJFMRwcOzUPcaMSCPKO/yjtxIaV+bzrDZahFc6jcWl5Yo4LwtbxRiRa+Jc8SK6EiuVgSFNCVYDKbrOd3Nho+f0bQ4pu4aRl8p1Zucc1d+X3pswtEKynGbdz5Pge8It3xSn8PZ5gHw612u7drS6ltHILxSMhI7KqSD/APjHqfouqQ65o1prVurLb3lrFOoPaFlRZFB84DAHz43HrHxJ4T7c/wDXUxf/AG3dG/x5adMi2/lRdfxuP9wcL25/mjbfwaT9+MOC6W+GFg6MGDowY5Zte15hx2zC37Jslvi6PndHi1JWwWCVUMCaaYGKk1YMGqRVHkrNSrxQjZkybEVdPHSpEUSHUOUo5+l6Xf6zfx6bpsbS3srUVR/SSewKBxZjQAAkmmMHUtSsdIsZNR1GRYrOJasx/oAHaSTwVRUkkACuFs8Hu7BGdw+27ex42YNNjSMMbVFeasuoX+JpVlsqt3Utpa+zrNOha7dY73l6nSX5gF/NMUUx9IFTpecwp3fdXT6TZtvavrd2nrV2XyrFGXVcmTMWdmQ0Gdfiox7aA040zbG/U3fcXK6Nat6rahMzSyBGbPnyhUVXHHI3xnUdlSK8NvsXdIp1axTlhukxgO2V+A4eagxy7UKhbUqbX9FVkHUdT3IzULCFs0lBPYVZ1d2XuSpZQxH7E4PEjeidPz40OwrmfVNP0qK8tXm1KAyxOmdo6AuMrNlDBqI2YZPC3hPEHGTNvi2g0y/1SS0uUi06cRSo+RZKkIagZipFXWhzeJfEOFMbn26+5FmXchqGk3XL6Fe6JE5rZoarP0L8avhIyT6XiVZYF2aNelZhsm0QRIBfE63nMYR+CAB4jjbz2TfbKuYLW/milknRnHLzUABpxzBTX9GMjaG8rLeVvNc2MUsUcLhTzMtSSK8MpIp+nE4dL0GtZLnGgarc3DlpT8ypNr0G2OmbRZ+8bVqmQT+xzrhowbgK71yjFxqpk0SAJ1DABQ9oh1VbGzn1G9h0+2ANzPKkaAmgLOwVanuFSOPdiz3t3DYWct/cki3giaRiBUhUUs1B3mgPDvwvXtw90zKO5SfZAzHM9Pz9LH39XTdOr41hFGU7GXEJ/wCJlmz6vScqxYTaQ1twLqPUVOKaZ0jpLLgKnpXHeuwtQ2R6t69PBMbkNQRlqqUy5qhgCV8Qo1OPEEDhWo7O3zYbz9Z9SgniFuVqXC0YPmpQqSA3hNVr5CCeNGgdUTF3wdGDB0YMeBKOHzSOeuY2P+NpBBsqqzjPe0WPvzghBFJr744KZBr6xg8POcBKXx8R65xhGcK5yoTxNK0Hlp344uWVCyDM4HAVpXzV7sV7Yj/ka8bJvbYvAmeAb2F5l9TZY+1WWVzckCFsf21KloKKyBbmdySICYVATLAgJwR+F6Yj8HpxSdFtbi0ttXa8tPVFtzMf7zNkCZ+zJStO6vb34UkfWHRpdTXSltLv1ppxEP7vLnL5O3P2V76dndjp+2d+3jXxl5OWbi5yFyHb8/tNMmq5EWu2xjaiXOnwre1wMDaYaxKGhbiWwvq+au2Ns8W92YLSCaZhIDQ6wCl1gaX0k1vXNCTXtGubWa3lViiEyI7FGZGXxJlDZlIFWCntzU44zdS6q6Noutvoer291FPGyhnAR0AZVYN4XzFcrAmilv8ARrww8aOkGMvHsZWLeNpGMk2baQjpBksm5ZvmL1Ejlo8aOEjGSXbOW6hTpnKIlMUwCA+A9Kt0eNzHICrqSCDwII4EHzjDOR1kQSIQUYAgjiCD2EeY48zrjjlg6MGDowYOjBg6MGDowYpdd9fU/n1zcWpTZwVSPxzNqfTzoJG86RZyeTd3+UcibwEPejsrWybqAURKX3UoCAHA/WtfVO/9a3ObZT4LaFE/1mrIT8NHUH4Me7f/AE9Noez3QJdelUi51zVbm5BPA8qErZxr/ZD28rrXieYTxUrhMXS3xvVgEQABERAAABEREfAAAPaIiI/wAHX6qliFUEsTQAd+OLukaGSQhUUEkk0AA4kknsA7zhalmlRnLFOS/iIlkZR87T8R8fKgq4UM3TAf5E0PKUP5g6+g3p1tobO2Fo+1gAHsNNt4X88iRKJW+FpMzHznHwm9fOoT9WOtu7OpJYtDre4L67iqa5YJbiRreMHyRwcuNf8ARUYwfVzwpMHRgxJnE470IOUkjF8Dv5AqBBEP6SDBEPKYB/k9Z0oH/SXpU79uuZqENqOyOKp+Fz/Uo/XhJdTbzm6pBZA+GGEsf7TniP1Kp/TjtPVDws8HRgwdGDG9ZfRZLUdMzvM4bxGX0W9VGixQFDxMMlbp+Pr7HwAQEBH3qQL/ABdY17cpZWct5J/dwxM5+BVLH+gYl9v6RPuDXrLQbX/xN7dw26f2ppFjX+lhj2a1fgourwMJWYRsRlC12IjYKIZp/wBBpFxDJGPYNifwfAQaNyED+YOtOZZXmlaaQ1kdixPlJNT/AE4947O0t9Ps4rC0UJawRrGi+REUKo/QABjL9deMnCfbn/rqYv8A7bujf48tOmRbfyouv43H+4OF7c/zRtv4NJ+/GHBdLfDCwdGDB0YMVYP+U7arZH8feLtOj13iVLs2t3GZtBEfVK0dTtVqTNCpN3qhPAh/BtZZVVNI4iBjJecA8UwEH50Dt7d9Yv7lwPWo7dFXyhXc56fpVAT56d+EZ1znuE0mxt0r6s9w7N5MyoMlf0Mxp5q92OMf8UL/AOe3/wBW/wD8jOpP8wX/ALR/5r/02I3oP/7r/wCW/wDUYb/3lqDXax23ufNxiEFEJjSq/mEtajeKHoPJKuXHM6nHvSlTbpre8DAxLVA5lFFPEjcgF8oB4dLjppdzT720i2kNYoHlCeYMkrkf7RJ4U7ThhdRrSGDZuq3EYpJMkRbzlXjQH/ZAH6Bis12zOV3InhX20+ZXIPBWWRu1KnvuPxliS02JtdhdOW1vjW9ZbhXIyvTlXZNHbB3IIrHXeOnKaiXnICBTFKczy3zt/Rtz740zR9XNyBJZzFeUUUDIcxzFlYkEAigAINDXCV2Vr2sbb2XqOraULcmO7hDcwMxIcZfCFZQCCQaknhXhiwrxG7nOwcrO1LyN5irVLMa7svHSB3htYYZ/XLHP5fd5fIcjYas19KuJXaFsEVG2aIsLJm5IMs49Fci5yAYhiJkTm4ti6bt/qBZbaEk76ZevAVYMqyoJpjEfFkZSVKsR4BUUB41Jbm397ajruxLzcRjgTUbNJ8wKs0TmKISjw51YBgwB8ZoansoBxPsYdxDX+bh+SVDkco4v4RE5dXqNO0yMwnJrDUawayXiRuCErL2euLaO++Of/QW4iRq6jlFPheZXxEDFlOq2zdN2sLK7S4v7uS4d1czyq7ZUCUCtyxl+Me0MPNiN6X7v1Dc3rtq8FjaxwIjIIImRczl6ll5hr8UdhX4cRa4i98rnfye5BaTjqtJ4qQiNDyner1GvWtN1WOQfyWXQTyTigmyudUtDp4xWVY+VRs0PHKHFUTe8gBAIef3F0q2noWjwakJdQczXFvGQXiNBKwBy0iUA8e05uz4vGogtv9T9063q02nGKwURQTuCElFTEpIzfSsSOHYMvb8bhTEvOyd3et07hGi7Bkm9U3OY2bpFJZ6LWLPm8VN19qvF/OFhXZeDnYubs1l9dym5mmqrRdudEPTIqVUpjeQ41zqh050rZ1lbajpEszRSymNlkKsa5SwZSqrwopBBrxpTvxYemvUHVN23lxp+qxwrLFEJFaMMopmClWDM3GrAginCte7GL17un8zoLaO4vnw5jQMJrPD7M73e8XsWgQUhOBszykzrAYWMkU39qrRrATWKKLqcihr4oHhWzc5HfvZxKJeenbB2zLpmi3nPmu59SnjjmWNgvJDqcxFEbLypKRvzK5yRlyjHXqG+txxalrFpyIrWDToHeFpFLc4owoDVlzc1KuvLpkA8WbEm+zT3Tbd3LKTsaekZrWaDouHvc/Sn31Hdyo020x+jN7iaJeRMPPvJeagHTF1RXpF0FZB+USKImKr4icpYPqXsG22PdWxsp3msroSZQ4GdDGUqCVAVgRItCFXv4Ym+nO+bjeltcC8hSK8tTHmKE5GEmehAYkqQUaoLN3ccUgqQ7SYdzGoPlyOTosudMA7WIyZPJF4dJvvzRZQjSPj0HUg/cmIQQTRQSUWVN4FIUxhAB2lulL7HkQUqdJYcSAONue0mgA85IA78ay2rBN6Rua0GqKeAJPCcdgFST5gCT3Ycr3E+2Hze579zbb9QxvBbhAY9oU3lkdGalqicdnEC0iKrk2fZ/N2hzDWSQZ3BaNTlau6WRaoRysoq29MRakOfyAtNmb62ttHY1rYaldxvqUKSkxRVkYl5ZJFUMoKVowBJYKDXxUFcMbd+ydzbq3rc32nWkiadM0QEstI1AWKONmIYh6VUkAKWIp4ammHp82OV3Int9OuAfG7DMkQ0HOLmFMx677daG7o5K9F01KpVP0W6yT5pVqjY1a6VeZWfzyi0b7qgsBU/Kiuugqdr7f0beC6vreq3BhvYs8yQLTxF87+Qs65qIFjAapHHiAWhuXXtX2k2k6NpduJrOTJC8zV8ITInlCo2WrlnJWgPDgSIo8ce9jtMp3MZ3gNsNTyrQ6O82q95FRdnzJjM1Kf/APJnE4pVbBOQ7y1XOs2JhKpRqLdwVieO9EFxcEOt6foK2DWul+mR7HTd2myXEN0LWOaSGUq6+LLnVWCIykVJGbNWmUgVqIHR+pWpSb0famoxwTWpuXiSaMFG4FsrMCzqwNADly0rmFaUOF7sPdy7hPbk5It8/h814wW3F79BEuGQ3Ox57rZZyQim65WFjqtgfRe4R8K6tlRkxIVyo1bIJLM3jNz6CPvHok7en3TvZ29NEN5LPfx6nC+SZFkhyg9quoMBYI47ASSCGWppU9e/eoG7tnayLSOGxk02Vc8TtHLmI7GViJgpdD20ABBVqCtA4DhFyO1Xlmye7e0tORzPGWyU2gu83RreaXOsaWW+TNcZzWjV21WGV164VV40zeSeJxKxmEYX15X3xsZRI0acXa43Toun7eYaU0dwuupLIJM0qNFywxWNkUQo4MgGcZm4Jlahz+FhbZ1i/wBfU6mr27aI8cZjyxusmcqGkVmMrqRGTkOVeLZlqMniYF1T8WzB0YMHRgxVH7eGQZl3AOd/NTb9fp8TpWatH85IwcPOJKqRiTy/Xx2GfOTAg4RUMrG0amPG6QeYQEDCYfaAdITZ+nWO7d1anqmoxrNZAsVDdlZJDyz+hEIGPYv8zW9t2flu/LvsPYGyb6bSt1PHEkssRAkK2dovri8QRR7u6iduHClB245R2++PeFcr+5HvgHzCrvOOVHaadYK3RxScDWPiktqjqVnzUqSS5VjuHEa+PJB4n8nqtzj5hHygbA2jpGla/vO7rBGdGiErKn7NMwSMfDQ5vhBxcfzJ9S+oXRz8rW3Cur3adUNQewhnu6jn8z1d7q8apFAodBB2Vyuop2kKV5TVyTtN13u54ZkNkb5g+vN1i8xYUioWKTg2sG5k5JlS2Meu0aPiOHS0Mgm4MUpzGUEqhygBQHwk+lu27fdnWbSLJIhHt99ZR2PxYhbQOZ3Bc+EZoYm4k9pxbevHUy76Rfku16+3Dqq3XVC32O8bZ5Ea7fUr2BLNZOSpEhCX11GKBeCgAmvHC0KXxR5LaJRdS06kYXqFkz7EyTBtYuEbT5lSBoR64z+MrI1sL9Rqmk0f1qK/tkk28TOI9n/aHBE0fh9e395ujbthe22m3l7bR395l5EZkXNLnNEKivEO3hQ9jN4VJPDHxz2m29fvrO51C0s7iSxtM3OcI2WPKKuGNOBQeJx2qviYAcccta57fn1YdXdlR7e8pbEypXtua1qacVhmZFVNBYrqfRZHim5kllSkMB1Q8pjAA+0Q6k2v7FLkWbzRC8bsQuoc9/Ba5j+rEctjevbm8SGU2g7XCMUHwtTKP14yEDlGpWqOQl6xmt/scS6dgwaykDTrFMRzl8KSywMkHsfHOGyzsUWyh/TKYT+VMw+HgUfDrn1TTLaQxXNxBHKBUq0iKQPKQSDTiOPnxzh03UbmMS29vPJETQFUZgT5AQCK8Dw82LM3YL4W5ntV415xyGziOtdLyLMY5pLVy6t3TJjB3262ZR+i5lkDqsV2slCxNSl0DEWEARA5/OUBKXw1K6z7pvLe5WTSJik1xckKyEHNHEoTwniCGJQ8O39OM/8AL5sHQOoXUjcWo7xs47rRdNtxFy5gQiTNJkRmFVIKx203A8BmJIrhl3GrKu3dzy42cmdJV4OwfHKp5V86mELpMbcZtwL1pD1KQsJ7hFzbdhWPi6Sq7dJJy/jXDZ+ySKqiCh3AHUTIvdYvt2bY1ezsxqTXc8+UmMoOBLBchFWqG4hWBUmhoBwOGJsXb3RTq9sbXtdbacWh6dp3NVLlZpDmCQtJzkkCxZWiADSRMsiCqZi4JUVUDVG1kWZtz1iwkXkIoJ1ggaFkirPoQfOATDNMWwHcxQimb+0EAyPwR+F7Ond6xAQTnSgbKeI4HyHz+btx55nTdRDKhgmDvHzFGRqtH8sCnFOHxhw8+PorVLQghDOVq3PotrH6/wA3nCsPIpoT3uxGyjn4mVO3BOU93TeImP6AqeQFSCPh5i+IJ4CWAdKp8biPD8Pk7D2+TH42n36JFI0EwSevLJRqSUpXIaeOmYVy1pUeUYaN2bsUkr13FsrYWGFeMyZAhbdPs0VKsnDKQjnFUhlo6vi5ZuyJLtHLK8TsUcQUIBg8vh4APgPVL6gailttKdomB9YyxqQag5jVuI7aorY2A/LFtWfV+tunw3sTKNME11KjqVZTChWOqmhBWeSI8Ri+F1rLj13wdGDCfbn/AK6mL/7bujf48tOmRbfyouv43H+4OF7c/wA0bb+DSfvxhwXS3wwsHRgwdGDEI+4JwczzuCccbBg18kXNafBJM7dnl6YNEpB9RNAh2r9pETxY5ZVunLRqzKUdMpBmKqJnLB2qVNVBb0l0rTs/dV5s/Wk1e0UOmUpJGTQSRsQSteNDUBlNDRgKgioNZ3btiz3bo76VdEo2YPG4FSkgBAaneKEhhUVUmhBoQp3s2dv7lr2xrxykrmqZrFalU9gZ5YtUNDyO/wBLViivM0c6OmLOZr+gy9DtLAk000EqgKptVwbnZnJ5VAOQ/TB6l7v29vq1sJ9Pna3uLYy545o3rSUR8VaMSIcpj7KitQeFDihdOtp6/sm6vob6FZ7e4EWSSKRKVjMnArIUYVEnbQ0oRxqMMb578e+Q3KPhBvGCwPzHdaXtQw7SEZOZ1WHo2cw8VY6nOJxq1kPXzz9qESVpU6zw0ekoq8fCCaCLZMhS0vaOsaNoO6bTV5eaLG1zFjlq8jFXWuXNlT4woMxAVeJLE4uO69I1fXNs3WlRco3tzQKC1EjAZGpmy5m+KanKCS3AAAYRvlPZS5s0Ttx8rOIUoOLOdD3PX8ZvNWmmWiTBqpHwtDftZGdLNO1qSjJovjDEJpoJotViqGcAImKBDdNTUOp+17veun7jj9aFlaW00bqYxnLSAhcoz0px41IpTz4WNh013La7Ov8Abz+rG8uriF1IkOUBCC2Y5K14cKA1r5sTM4L9uHlZxg7Y/MzhbdYbNpnSeQX7d/mXP1vQHC1PZftgweqZHG/HzqRq8fKtPiOVryjt16LRfztVCgl51PEgVnde9dv67vrTNz2rTrZWfIzq0fjPJneY5QGIOYNQVI49vDFj2vs7XdE2TqW27lYWvLvn5GWTwDnQLEMxKgjKVqaA8OzjjB9lntmcsu3BaOQczsMVmVsaatV6Uxr5c+0B08WaSNKXt78zaUJPVWCKilLKTqKSSiYqgmIGMcAAA8e7qdvnb29YLOLTWnja3kctzIwKh8gqMrt2ZSSDTzY6um2yte2dPdyaisEizogXlyE0KZzQ5lXtzAA8fPiJnBzsqc2+MvJPT9muhcWmK9d8j5A0SNjq5o0uvLtpjU63KR9eWeEkKTHtCsW71wmV0cipzkKYTFKfw8OrDurqftbXdEg0y19aWaK5t5CWjFCImBalHJrTs4fqxAbY6a7m0TWZ9RufVmhlt50AWQ1BlUha1QCle3j+vHeezP2mOXXb83nTr7si+XPa1o+Rvs5aSecXl7MTVclFrJA2FCZVjZ6nQ7Zy3SLCimUCGUMCpyiYgkA3UR1L6h7d3hpEFppgnE8NyJCJECqwysuWquSPjebh31xK9Odg7g2nqs93qJgMM1uYwY3JKnMrVoyAH4vn491MRm47djnnli2g82GVjsWK6ZG8kOLGzYRBa3PaVaY1d1YtLttEsrK72KHUodrtHxr5aqqL1uYDl94cD5HqpSgopO611U2lqdnpbQJdQPZX8M7QrEhosSSKUU8xFp4xlPkHFR2CF0jpjurTbvU1me2nS8sZoFlaRhVpHRg7DI7V8JqPKfjHtM5+x/23uW3brT5RSG2R2Xvm+sMs1JWYGo3R3Mz72WzQ+hGROs9VhmEDEQssjfDAVRVVZz6iIeZFIoeY9U6p7127vM2CaW04a3MuZnQKoEvL7sxYsOX3ACh7Ti0dMtm6/tAXz6mICLgR5VRyWJj5nfQKAc/eSeHYMK/q/YE58wXLyu8h3C+CLViG5IRG0LwiOlz3x8rAx+nN7wrFJAegEj/jc8eiKJQFYEfWH2n8vwur3P1e2jLtx9GUXYnayMOblLlzGLJX+8rSvHsrTuxSIOlG64twJq7G05C3gmy8xs2USZ6f3dK04dtK9+Lrccs7cx7Fw/ZfFr5dm2Wex3vKTz3B2qiQ7ll72iBUXXuqxjJ+oQAKfy+YPYPWsDhVcqhzICaGlKjuNO6vkxsmhYoC4ysQKjtofJXvpis33UO0py/5R858w5V47a6PfKFXTZc1UzW8W9/UpCiJ0WYSk5dtBCtCy8K6rljXQO8VVTOD0r52qUWyiZSHF47B6h7c0Hak+39Sjlhu35p5qIHEnMWgLeIMGX4oB8OUDxA8MJbfWwdw65uiDXdOkiltU5Q5buUKZDUhfCVKt2k/GzE+Ejjjl8D2dOcMT3bw5s+GBs84kd8sG4OF0NCs0wEI2szmVduIL4qPQq3O2GciVZIRKmCMe0fmIBTPGgKGVRz5upO1ZOnfsv8A4s3q2iwf3armKgDNXmMqqaeVivyWpQ4MXTvc8e//AGl/wgszdtMfpGOUMSctOWrMwr5FDfKWtRNzuqV/iL3Is50PhvSdxzJzzJwy31Oco1fM/eqStbu05aIKgyVTfLsmKqchHThbQnGTCLVVdOEkDtHMkVAGniWr7Am3Fsm9h3LdWs421dxusjUFGRVaQOKngVy5kJAzrmVK5sWbfUW395Wc23ba6gO47WRGRamquzLGUNBxDZsrgE5Gys9MuG9ccsKpvGXCsqwKgJnLVMqpkPUo5ysmVJ3LLsUPPL2GRImYyZZWzTSziQd+QfJ7y5P5QAvgALnWtWudd1a41e8/8RcSlyO4V7FHmVaKPMBhg6PpdtomlwaTaf8Ah4IwgPeadrHzsasfOTjtXUXiSwdGDEVucWpmxbiFyJ0hF0LKSgsrtLSAdgIgLa1WVkNVqS3sEphBOzTbQRABARD2AID7eoHdF/8Adu3ry9Bo6QMFPkZhlT/eYYcH5f8AaA351r2xtaROZa3GsW7TL8q3gb1i5H6YIpPL8Bwl/sWMI6Y4kcsK9Q7PXo7dbJaJ5hHsHrz0H0THFzOMY53Z5NJqC0mWvBcJuTJ6ySJ/TOgoBfE4gUVt0sRJNv38NrIi6q7sACeIHLAjY045c7NxA7jjez/qE3N1ZdaNm6nuK0uZentraQu7qtUkf16R72CMtSPnerRQHKzCoda0AJxquLZU+7Q/ETlJdd3t9IYcit3gEqXkFCrE+WcnirMImyxddkkPSQQWWRJPWVaRkDpJi1btI5EpnAOViolx9NsG6e7ev7rVZIhrF0mSGNWzNwDBT/tMWanABR4sxpiY33vC3/Or1q2hoPTyy1CTpjt25N1qV5PDyoaPJBJMhqSATDAsMIY8x5J5CIuUhctSyNxe8Ttfb94fVZw0YR0BxovN33lieNZunDiOzmq5tRIlJN64J7xEOJfW9FF55m4lUWKwWTEBT8/g4tt6b6joq28o+jtbGGM07riR0yV/0WjivDTh4gp7qHz36t7isd57p3FvsVe81jdkz2rEmhsVW8knygEAvE0ukoCc1I5GoKtULPt5M4fcHOQGg6DJyFPqnck7lxc9GUWt89AwaWN33lDVcUcWxxFlmGFdj3L3BMrkpc746KaiyJkzLKnQIQA2TtDqCbzsLCwVZbrb23eblEas3rEVm9wEBylyBczImUEgGtAGJxpPdCwfZ97fXrNFba/uDlVzsq8iS7S3LkZgoJtoWfNQVFKkgDDGdCvMpxJsPIq/6y2pGXduXBOKWfU3KM0RRpDSBvl6Fa4vbZHQsIm0NLNXQQiENU46FVAGsio6J7sgqcyohQLCyi3TBp9jpZmueoF9qksk830haKP6MIWauUjMZJ3kHiQKczAUxe768k2zPfXupiG22JZabEkMX0YWSTxlwq0zA5RHCkZ4MSMqk1xrHHgmiceZPth8H6uu3iWVZ4l3i+8kIosPHLLqoZrR8vokc3UeO2hnMMM5tmoKvvM0Mm5WPFqJqmFIyhVMjXzYa9HuTedyC7yapHFaNmNKzSTSk0Bo2W3hC+KoGcECtKY+hi+0OTb20LchUj0ySS6GUV+ijhjAqRVc08xbw0JyEE0rXgNC2RLj5xI7knPuLgICVfalyT1mRztnYmrh3W7JXKvc47As1TnmDJ9HOpFg+uBJFd8kg6SMomudIihRKKg8LrTzquu6Rtd2ZVhs4hIV4MrMhnkykggEJlAJB7Aad2Edo+512Z043z1it4YZJtQ128a2WQExSxRTrp9rzFVlLK03MaQK4qGKhhTNiIPbx7hexc8eUtFwLYmuN51icJWbXdUMey+nDU6vodjrKCDmBrkohP2CzSshGxsg+VnzxrZwk2dLRvi5SWQKcnU/uvamn7Y0WXVNPNxLqLOqc2R8zRq3BmGVVAJACZiCQG4EGhwtOi3Wjc3V7qBabO3Mul2W1YoJpxZWsPJiuZYgDHE4kkldlVmNwYlYK5iq6sgIxPjlZq/I3Lu3n3BL/vhE4Gbtuj6ZneFV9onXE3Vbwm9y9Wx3P25XNdHzrSK8fIyk2Lh0qZ8X3rz+CfgmkWr6HY6Re7r0q10vxRpDHJMxzUaZA0r/ABu6oVKAZeHfxOHD1D3Fvfb/AEW3nrO8QIbq5vrq2sIwIqxWFw8VlbisfaxVpZ8znmDNXw+FR971yOy7De3nxf52W2NZzey0vizBZ7h7WRUUURdabs9FzxGwtUmYiUiwovM4By7cgZNckKzfJpmAzgSH/LbSL3Ut13u2YGK6fJes8xHdHC8mU1+CSgHZnKk9mP3V976BtLotoHVzUkWXdFrt+O2sAxJBur23thIAvfRrbM7cGECTKpq9DB//AI9FPsF4ufLblJd3zydtFkkYKlmssgAKvpqcs0nJ6Dozt468hAFy6fJwy5wJ7DGUERAAAnjZeq1xFbW9hotsAsCBnyjsCqAkYA8wzjCo/Jdpl5q2qbk6gaq7TahO8cHNbizySs1zclj5S3IY07zxHZi0B0mMb9YOjBhPtz/11MX/ANt3Rv8AHlp0yLb+VF1/G4/3Bwvbn+aNt/BpP34w4Lpb4YWPwcrptWzh0qdNNJsgquqosqRBFNNFMyhzqrqCCaKZSlETHMIFKHtH2dfoBYhRxJx+EgAk9gxDH8duA/XBxu/NHi30h6mvZncf4fe+gl+biG9o9v8A26y9PF87B+O3Afrg43fmjxb6Q9HszuP8PvfQS/Nwe0e3/t1l6eL52D8duA/XBxu/NHi30h6PZncf4fe+gl+bg9o9v/brL08XzsH47cB+uDjd+aPFvpD0ezO4/wAPvfQS/Nwe0e3/ALdZeni+dg/HbgP1wcbvzR4t9Iej2Z3H+H3voJfm4PaPb/26y9PF87B+O3Afrg43fmjxb6Q9HszuP8PvfQS/Nwe0e3/t1l6eL52D8duA/XBxu/NHi30h6PZncf4fe+gl+bg9o9v/AG6y9PF87B+O3Afrg43fmjxb6Q9HszuP8PvfQS/Nwe0e3/t1l6eL52D8duA/XBxu/NHi30h6PZncf4fe+gl+bg9o9v8A26y9PF87B+O3Afrg43fmjxb6Q9HszuP8PvfQS/Nwe0e3/t1l6eL52D8duA/XBxu/NHi30h6PZncf4fe+gl+bg9o9v/brL08XzsH47cB+uDjd+aPFvpD0ezO4/wAPvfQS/Nwe0e3/ALdZeni+dg/HbgP1wcbvzR4t9Iej2Z3H+H3voJfm4PaPb/26y9PF87B+O3Afrg43fmjxb6Q9HszuP8PvfQS/Nwe0e3/t1l6eL52D8duA/XBxu/NHi30h6PZncf4fe+gl+bg9o9v/AG6y9PF87C3snxjtm43zM0jnRVdGx13tGjydtsJ2Vh5iYVK0mmWrQFnS99tdIiAXbTMdO248k8Byd3IvkUE3y6bRNskfyBdtQ1nqJqe2YNqXFlcjTIFRarbTB3SOnLRzQqVSi0oqklQWLEVxTbDSNgaduObdEF5bHUpmdqNcxFEaSvMZBWoZ6mtWYDMQoANMMh/HbgP1wcbvzR4t9IeqT7M7j/D730EvzcXL2j2/9usvTxfOwfjtwH64ON35o8W+kPR7M7j/AA+99BL83B7R7f8At1l6eL52O41Db6JdM2seowdtzuVqlYQn15Ww1/TadZKbHp1yLLLSZ5i8RD9etwiTFmcFXZ3KxCs0BBVXykHx6wZtN1C3uVsriCZLx6ZY2RldsxotFIDHMeAoOJ4DGdDqNhcWzXkE8L2iVzOrqyLlFWqwJUUHE1PAcThS/f01IalxGqWbtXIpv9b1SGbvWwKFKDmr0dg9s8kYxPadUELKWGHw8PKAj4iICAAKh6sX/q+3o7JT47icVHlVAWP+9kx6I/8ATj2j99dab3dMyVt9F0eVlanxbi7dYE49grB615+FAO0inmzevY5ym8j3bpg8R8/ou2bhVq5S9RMySnproHIqTzpHMUfAQ8SiID7B611VmRsyEhh3jgce3U9vBdRGC5RJIGpVWAZTQ1FQQQaEAjziuPl2/fP3Z37567evlBTMo8duVnDs5kiETSE7hY51jCkmmUpfEfglKAB7ADr9Z2ds7klvKeJwQ21vbwi3t40jtxWiqoVRUkmigAcSSTw4knET+QWj25W6xqDe2WQjqJgSt1Hac5JkdF+MnRnarT1yugW93FNugp5PN5RN7fDxDx69XfyM7WW36aahuW/QPNqeplULDNWG1jCIamvZNLcCnd214kD5hf8ArO7/AIb78wmhdONEIhsNu7dEsqxgRhbzU5mllGVAK1tLawYseJqBQBQTHVzYbA9j0oh5OTDuKbnKohGOZN6vHoqF8/lUSZKrnbJnL6pvAQKAh5h/lHrd1beBJDKiIJT2kAA/rpXHjq087oIndzEOwEkj9XZh4XHftbROtcVofeOZnO7POK9ftDEhOLtN0y8V6aZ2xi4hn8ui9kUXd1K9oMI+kFUgBFtHOnyTcVnKjbwOgVZM6/1Ll0vcz6JtHRLjU54m/wAZJDGylCGC0FI6SsBXiXVSaKG4NRvaF06i1PbiazuvWYNNgkH+EjlkVg4KlqmslY1JpwClgKsV4isweMXZLXvGZr8hbHzsnbCyJqUlnWbWzj3l+rci6do1Wj2cOSZssCrUXLO9OWMVa0LBHKgEaSOTeQZVVHIJqeIUbenV8QTnbdvoywu9sskq3MkVvLC5YkIysDGC0fLYHPmyy0C1GO286Mvre05rybXp0E10IIprK0utQEkYZGdkS3+mIqs0ZOURqUqzgGo23mx2u4jjPlOKuqHyD0zT7TvuswOZ55lVtzGczBVy6mlHyzqWk4GxzRp6KUaTZmaPu7iMSXOvIpqeHl8TDSNub0k1i+uFurSGGC1gaSSVZBJwFKAMoymoqahiKKRhH9VOgFtsTbulSaPrN/f6hrGox2ttaTWslqSXzEu0cr8xCJMi5WiDFpFbs44lKy7FuPwOh3Cst+bt3iLVktNjdCtjqOwKws0auxXTcP0pFG8I2lKsmfIsG4Llj2jxaWIkYFTpETEgnhW6l6hLaRzHTYmgnkKKDOpzHspky5qV4ZiAteFa1owYvyk7as9aubBN13cWoabarczFdPkURKQWDCcSiLMFGYRo7TAHMVAoTgbD2YkNBHF7FSucqOh5rfJdpN6M41VGfpNsgKS5IhOuJKHq8o+sToLcYiztFdhPBCrM5NRJNfwOZcEuyLqEbX1iK503lXkSkR8rK6s/xaFgFGXsIZM4K1I4Urh3v5Xk1k6Xe6Vu0XuhXkgkuTdiSCaOA0kLJE7SHncXDR3HIKSlVfiXywd7uPJ6taxstZ4942QkTxz4mQCOWZ3DMU1G0bIzsa0YxlnsSSaiaazlqgWLbxbFRQVCqNmJnSRgB6p5rLsTRprHT31XUPFq18/MkJ7QpJKr8PEs3ZxbKfijCn/Mhv6w3HueDZm2AI9kbchFpbIoIVpFCrLIAQCQMixRk1BWMyKfpWrZS7JGUjmHb5zF+4bC0ldXsF01aVSMmUhjBMTA1qvuRMHgKwPabU4xYph9vkUAv8BQ6UHUa+9d3VMoNUgRIh+gZm/U7MMb0flU279wdGbCZ1y3GozT3biny35UZ8+aCGJgfIad2G1dUXGxuDowYT7c/wDXUxf/AG3dG/x5adMi2/lRdfxuP9wcL25/mjbfwaT9+MOC6W+GFjHy8Y2moqThngqA0lo97GOhRMBFgbP2yrVcUjmKcpFASVHyiICAD/EPXON2ikWVfjKwI+EGuOEiCSNo2+KwIP6eGK/f6Z7t0fLfJH7TKt93HTf9+O9PkWXon+swpvcts/5d76Rfq8H6Z7t0fLfJH7TKt93HR78d6fIsvRP9Zg9y2z/l3vpF+rwfpnu3R8t8kftMq33cdHvx3p8iy9E/1mD3LbP+Xe+kX6vB+me7dHy3yR+0yrfdx0e/HenyLL0T/WYPcts/5d76Rfq8H6Z7t0fLfJH7TKt93HR78d6fIsvRP9Zg9y2z/l3vpF+rwfpnu3R8t8kftMq33cdHvx3p8iy9E/1mD3LbP+Xe+kX6vB+me7dHy3yR+0yrfdx0e/HenyLL0T/WYPcts/5d76Rfq8H6Z7t0fLfJH7TKt93HR78d6fIsvRP9Zg9y2z/l3vpF+rwfpnu3R8t8kftMq33cdHvx3p8iy9E/1mD3LbP+Xe+kX6vB+me7dHy3yR+0yrfdx0e/HenyLL0T/WYPcts/5d76Rfq8H6Z7t0fLfJH7TKt93HR78d6fIsvRP9Zg9y2z/l3vpF+rwfpnu3R8t8kftMq33cdHvx3p8iy9E/1mD3LbP+Xe+kX6vB+me7dHy3yR+0yrfdx0e/HenyLL0T/WYPcts/5d76Rfq8H6Z7t0fLfJH7TKt93HR78d6fIsvRP9Zg9y2z/l3vpF+rwfpnu3R8t8kftMq33cdHvx3p8iy9E/1mD3LbP+Xe+kX6vB+me7dHy3yR+0yrfdx0e/HenyLL0T/WYPcts/5d76Rfq8H6Z7t0fLfJH7TKt93HR78d6fIsvRP9Zg9y2z/l3vpF+rwfpnu3R8t8kftMq33cdHvx3p8iy9E/1mD3LbP+Xe+kX6vDG8K7cPHzjzxI1fhdQXuiLY/skdp0ZbnNgskZI3JNtrVNRo1pGHmm1ej2LJVOFQKLQTs1gRX8TGBQPg9UvVt66xrO4rfc92IRqVs0RQKpCVhfmJVSxJ8Xb4hUeTFx0vZ2k6RoFxtu0Mx0+5EgcswL/SpkahCgDw9nA0Plwgj/kD6n85eSmVZS2X9ZjluXKzTtMFPEGtk0iaUcSDcUgMYEzGr1ViFfMIAYwKB4h4FKI6m9XL/n61b2CmqwQZj5mkbj/uqh/Tj3V/6bG0PurpXrG8ZVy3Gr6uIlNPjQWMQCGvf9NcXK04gZT3k4Qb0p8ejeDowYXVocr8dXazSAG86Z5Vw3QN4+IGbMBBg2MHh7PAzdsUQ696OhG2fZHo/t7QyuSZdNilkFKUlua3MoPnEkrA/Bj4kfzq9RPep+a3fm80k5tnLuG4toHrUNbaeRp9swpwytBaxsAOwHv7cab02cavY9habk/hHBrtvcMqk45X8iahGbTWakz456JW8Dxyb2J3R4+Gr0rGxLDM5LNJOk/ETqFlo9IH0rGyU26CVSVWdrOlvXLoaNt63vTqDq90ul6fLJZyObuJ7m4WASFmUsZhMJMwZWOVHSNchAQKMuN4PaHRtn7C0q1bUr6KO7jQWsqW0DTmMKpCiIxGPKVZRmdHkOcEsWNcTnZ3h3D3mscVbLbH7m4Z3xae8kdl0x9YbTXHLy2zFvTh5F1MK47NZtDtErFPhYZZ02bNk2TRIqIM2KKHpkBe3ECTxSa/bRKtpNferwRBVYCNVqoHOErHKvLQMxLE1zOWqcZt7reXcUexbiZlubTQn1K5uDLPGcxmEQzG0ltwFdhcSMKZVCrkjAwu6v3Suc/O6RxfXpN7jNbxnh3iQ6baZGrx9pWokJskh4RqbOMttq80vZZNzaWkDJN3boQVcNoo6fgdds8VGzy28219l3ouYmg1DULnlqGK5zEONSq8FGUupA7C1ewqMIWy1Sy6xfmA0B9KvI9S2vtnSvWpWiWU28d63hASabxysZRbyq7cWWIrQukrYljvW2tanxR0zVJGY5U6BA8lNzicyq9ILSG9N0eoQFiuClNdUzMqpNxDVeHjJqEg3qDF3OAc0k6eJreUxXCKB4LS9OafXIbJFsYpLO2MjPnzxsypnzyMCakFgSE+KAR3E4Yu8N1x6d07v9wzy7hvLPXdWS1ig5AguYY5JjAYLWGRAUV443WN56813VqEOqHaeXGnJcReH0RYbpZOWdiao2WILbrZS9DzqR26pTE40UmG0BY7I6hfmspXGZ1fdF1GLQSETTSOBjlV9Q/ToNmde19ordLFGKHKrpIIWC8MyqDmzHtFT5eylBIdSNeXpt00ivdUn3HPGJ0500FzbNfQvIC4jllKcoxLXIxjSgAUgkNU0VZiVtuq3+TmpFzK2q9aNbnci8dvnIv5qxWq2zCjhZZ07UBIXclKyr8ROcQL51DiPgHj1svGkFjarGgVLaGMAAcAqqO4dwAGPJG5uNS3DrEl1O0lxq99clmLHM8ksz1JJNKs7txPCpOPZX47njDI8lzDKosUzR2a57TaGzOl5hIq3qVdjoFNcDHKQ5xXKw84mMAGMJhEfaI9afahdtf3818/x5pXc/6zFv8A9491ds6LDtvblht63pyLGygt1p3iGNYwf05a1PE9+Oj9YmJvB0YMJ9uf+upi/wDtu6N/jy06ZFt/Ki6/jcf7g4Xtz/NG2/g0n78YcF0t8MLGj6ZpFLx7PLrqujTjetUPPKzM3C3TzpNdZGLgIFitISTv3dqms6dKEboD6aKJDrLKCBCFMcwAOVZWdzqN5FYWal7qZwiKO9mNAOPAfCeA7TjGvby20+0kvrxglrChd2PcqipPDifgHE9gxTu4891/mpb9T5icga7AzVyyzeovS3WCVuX5O4xEteM8JQ63ONGN8ZccbCwtlwu1gr0FSI908bs28XGOzt5J2sk7B6ZwlsTrGwttW1jp2kTOsd/aNGJ2FtMTcs7KShuFKIiszsASWYVRQVy0Ovukb63HcX2oatEjSWN0shgU3MI9XVFYBxbsGd2VUUkAKpo7ENmqOBUTu59yl4w4+2iS5ja5MqaLqeZ1m0Ved4K4HTs1PDWayFYy7aC25vWXJZ5ZRol5Efd4xkqqCihyHTMj4mlrvp9stXvIE063UQwSsrLfTvJVVqC0OYZePbViBwqDXEVa7+3iyWk76jcMZp41ZWsoEjozUOWbL4uHZRQTxoRTDK+75z15z8IuWKsBV+WdDzHDtQyG26Zkddf4FAaFLQtlz3PmUe1zOTlfmxIS67vXNXilkmko5VOyhU5ch3JiNm4lJSunu1Nr7n0DnT6fLPqkFwkcrCdowyySEmQDMBSKIglQMz5KL4ji5b/3TubbWu8qG/ih0ye3aSJTAshDRxgCMnKTWWUGjHgmep8I4K/lO7R3NaIlPWa/2PbNUieVXGKOy+gy7PHy5dQKBr+nUxKz5zYuP0nSxCLul9gC2dRu8ckSaybp4BieksSPZFJeI9gbJuisFoltBJYXxlkBm5skkMT5ZFnD8UjbLUCpUDjUF2rSn35vO1DT3T3M8d9ZCNCIuXHHLImaNoCnB3XNQmgYnuIRaWCcY7j9jxHtJTPKnYM35M3vQ+NC1CzHU4zkfWIPEtL0y72SwZTESlmgAZIWJFagxi2vIt4ySetCycijDqA8KZ2KrpRSajsyHVOoC6Dp01lFaXueSI27NNHGirKQrVy+M8ollU5VLjL4aKGtp28JtN2G2uahDey3dlkjlFwqwySOzRAstM3gHNAViMzBDm8VWK8d07qnO6e5k8B5uvcMuRuV1ywxumTDLjc31uMM35YxkjU2b6MeoHZV9lFejSGy5JAovWy4iVQPJ5De3q4aVsPasW29Wjm1KznmRogbjlH/AApDkEcWJ8Z8PAjsxUtT3zueXcOlSQ6deQQushFvzR/igVBB4KB4PjcQcOn7fXcUmubl65N5ncON9r4233i9JZpD3Kr265x1tk15PRk7+qRkqnHV6DTi1oZOiiJwEy4LA7L4CTyD5lru7Z8e2LWxvbe9jvbS+WRkZEKCkeTjxZq1z+alPPhjbU3dJuW6vbK4s5LO6smjDq7hjWTPw4KKUyeetfNjUeU2Y909zpup6Jh3NrCMQ46RjCNmqxULdh0ffLDWIKv0GBUu0jOzi1XdPXZ3dsYSz5JJNR0KbNVIpTAP9SnkaFfbEWxgs9U0y7utYYkM6TmNWZpGyBVzUFEKKTw4g/CejXLLfBvZ7vTNStbbSFAKo8IdlVUXOWbKSasGYDjwp8AT5w15Wd7jmbs7cuGbVUNB4r1i5xzG08k7nx4p+QZ5boeKk2nzrjaKzlqe9u85KLsirIN0kGabluqYgvix/mAwMPceg9Mtt6afvS2kh154yVtkuHmkRiDlMhDhFANCamh45c+F9t7XepO4tRH3Zcxy6GkgDXDwLFG4BGYICpdjSoFBUGmbJiwBzE5S8g+OslQ2eJcI9O5ctbUxn3Vhkc+uMLVkaO4iV4pKOZSictCS5nis6m/WOkJBIBAamAfHxDwU23dC0jWElbU9Tg09oyoUSIzZ61qRQimWgr8OGruDW9V0h4l03TZ79XDFjG4XJSlAag1zVNPgwkCod0fuDrdxHWYxTgjyKscYz48Vddvw1T0usJOs9curDX/U2RzJjVRaOyTxU/c00wSA6fvZvheAj4s642NtEbPt5Bqtmjm8Yeuct6SAK30IGaoy9p8tMLW33tus7tnQ6XePGLRf8JzFrGSy/Sk5aHN2fpw8/iVyg2Xda/pFg5A8Sb1w1b0Q8SrHBqFzgp1vaYl0ymHs5NIP2MVCNolhWCRifvJ1xMXyuAN5igUelduDQ9N0uaGHSNQi1Iy1rykZcpBAVaEkktXhTyYZug61qOqRTS6rYS6cIqU5jq2YEEsagAALTjXy4Qlw875WAjy95w6hyc5b3GlYRbbpFw3GTHXuc6Ddqm1q1WbNa2XQW6lPolymqfK2WEgGbkYxBw1ZLO38g4doqOBbKA19xdLtWG3tLsdE0+OXVY4i1zMJI0fMxLcvxuiuFLEZiCwCoFIGYYVu3upmlff+p3utX8kelySgW0RjkdQqgLzPAjlCwUHKCASzlgTQ46jjHe/4/wA53JN8Sm+WlgsPEW40fIavxpp/7IbulHO9dniUiFtqcYX9l8VeYUG8u2enUUn1E2KhnyxkVfIVEgYGpdMdXi2ZaGPT0TcEcszXL81KiJc7JX6Uoagj+78XhFRWpxnad1K0qXeF0JL930CSKJbdOU9DK2QNT6MOOIPx+HE0NKDE9u7pzK0zBKFk3HvivLCXmtyq0uo0rEmbBpFzDusRLC0Qzu1Xmdi5VlKxqVbFEhYs6j1qo29J45XEDJMnAkqnT7bllqt3cavrq/8A1mwgd5iSQGJUhEUgg5v2qKa1AHawraN/bivdKtYNJ0Nv/sl9MiQgAEqAwLOwIIy/s+IU4k9imnEdh5Fb1wZ7lPGuW5FbjL3fhzyxxyIxZSakG7Kv5vmXI+vsK2k8twRMUKENXmt/l49s8B07VVK3bT8iBDFaR3iST07R9K3Rsu9j0e1WLcdhcmbKKtJLbsWolTxYxgkUA4lEr4n4xuoavqm2d42cmr3LS7ev7cQ5jRY47hQtWoOChyAak8A708KcOs947me64rUHDaxVuWp+G+galfbBMtNMWwRDkFHzGfZzAIsbvUDVZ3WrS2jpV3P6LXnrVyKCBjkYLJA4TKYxVMDp1ttddu7qefT/ALxtIIlBj5/q5EkjVR82ZagLHIpFT8YGh7s/qFuJtDtbaCC/+7rqeViJORzwY41o6ZSrUJaSMg0HxSKjvRD+9b1r/wDfl/8AyzrP3ddNP2C0/wD4U/8AlG+swsPbm/8A+Kf/AI1fq8TN4N8+uYV3lO43X9m5muNGqGIcG43kFkG2Jccsmqj2mfPrHmur1/TRy6tVeNVsMjWIKwtlzwEm8eNnazQUTAAKD4VzdG09u2sejTabpvJuLrVDbzQ+sSuH5c3KaPmsxyhmUjmKAQDXuxYds7q3BcyaxDqOoma3ttME8U3q8SlM8QlWTlqozFVYHIxIJFO/Ck+4jqf7ZebXJK8JOCO2A6ZMVKFcpG8zdzAZ2Rvn8G8bfwACD+LrKTgvsAR9URMHmEevNDeF/wDeW5r26BqnPKKfKsf0akfCFB/Tj6wPyybQ9huge1tvupS5+6o7iVT2rNelryVW86STsh/s0HADEL+q3h7YwlllAhK9Ny4iADGxb54mA+AeZZBuodBMPH2eZVYClD+cerf0/wBttvDfOkbWUHLqGpW8DEfspJKqyN8CIWY+YHCr659QY+lHRndXUpyofQ9v397GDTxzQW0jwRivDNLMI41B4FmAJGFqCImETGERERERER8RER9oiIj7RER6+hFVVFCIAEAoAOAAHYAO4DHwjySSTSNNMzNKzEsxJJJJqSSeJJPEk8ScfHX7jhhqT7vS9xR/i1ewNXbY1LP6rE59CQaTXM80azbSNy+Ur0tTmys0jVSu3xGS9WZpuAceqD9Ah03QLFVVA6zTpBsGPWJNcFmxv5WlZqzTFSZldZDlz0FQ7EUplNCtCBRjv1Y30+kx6KbtRZRrEq0iiDAQlWQZslTQooNa5hUNUE1nfUu+P3L5aHrs7LbbBe/GZPlFm7fJ8vbx71vJKpqNhdtAqYlMq1RbpGSUIJFCiY4eYSKHKZLa50x2Pa6nPaWVq4t0YKKyykgqKNxL97V4fB5MKfc35pOsNruCWHTtSijtYGKBfVrYhjRQ2bNESSHBykEEcR2Eg8Jju4HzBgdB1LUatt9nqFz2icj7FpEnV28PDksklDtV2MMK7RvG+7t2sOycqItUEikSRTOYClDxHx5vtXQJbWCymtkkt7dSsYapygmp417SeJJ4nCmg6y9S7PWtQ3Bp+rXFtqmqTLJcvEETmsgKpUBaAIpIRQAFBNBiR0d3qO4nGtIVoTao16MFFDFtXcrnWfyj5yIoNG4ycm6fV1ZSRmhTaj/alfMp/XLB/AqYBiH6d7Tdmb1dhmapAkcDv4ABuA49g8g8mLvB+aXrXBHFGNVRuTHkBe2t3Y8AM7FoiWeg+OanxN8o4ju97g/LyWzXacmnNgk5+mcgLG8tent5mHrr+VmJuTCHSlVWE6pEhLwTGRZV9k1MyZrIskmjcqSKSRBOBpZdq6DHeW99FbqtxaoFjoWAAFaVWtGILE1IJJNSTilS9Z+pNzoWqbcu9Tkm0vWZ2mug6Rs7yNkDlZCmeNWWNEKIVQIoVFUE127tcZT+2Lnxxpq6rf3iPh9AbaJLAb/wCsMvYvdB8jr2CBm7x5XEWwlEPBQVgIPsN10b0vvu/a95MDR2i5Y+GQhOHwBif0Ykvy/7e9pusOhWDLmhivRcv5MtqrXHHzM0SrTvzU78ewq61Ux7QYOjBg6MGE+3P/XUxf8A23dG/wAeWnTItv5UXX8bj/cHC9uf5o238Gk/fjDgulvhhYwlmjDTdbsEMVrEvjS8JLRhWU81TfQbwz9g4ag1mWKqDlJ5EuBV8jhIyahVERMUSmAfAe2F+VMklWGVgaqaMKGtQeFD5D5cdUycyF46KcykUYVU1FKEcajyjyY9eTecT2njHzh5FUXRJzgRQddjszSNLxdVrd5YVWchtOyqfkrXEcbIVnnLb5v6FL52/kUHK8i1iIdB66TRK5IKyAG28tdT0zW9r2d1Zpq0unmfgXZCytFKoU3JMnijEgUgKXcgE0NDjU2503UtF3Nd2122lRX4h4hVcKwkiYsLcCPwyGMsCWCICQKioxw7E8U0O6bLw9wyjbHW5RzsMdhWqZevqe2PmmR5RdoT3yYted2ypRSz4h7nKSzNBSJiEis5cI6YYkRQWNIoOepTU9Ts7bTtR1S6tnUWzTxS8qEGWVGoqyI5p4ACc7mqZkapGQjEZpum3dzqOn6ZbXCM1wsMsfNmIiidalo2UV8ZIGVBR8rrQHODhuf/ACC93pyvMCgVaC3GuUq9Z1xF3/ONSRbUNW+HRntPoMi5jcpWhpOKkPm442Gu2RFixnQEDwSMmnIlXSOgVQF90k0q5G3ZriW1eW1m1C3ki+k5fhikAMuYEZuSylmT9sqUoQaYvvVbVLc7gigiuUjuodPnjl8GfxSIaRUIOXmqwVX/AGAweoIrhVWq7xywDinwBbT/ACNts5Xs+tK2h4rirjjbncJYMbrnHyFghhdoi59g0WmdUoCLWVkEY5xNnKzkRg5BRyXyICp1fLDStA+/tXaKzjWaaPlzTC5kZZmuGbNCVPCKTgpYJxXOgXiaYo99qmu/cWlCW8kaKKTmQwm3jDQrAq0mDAVlShYKX4NkcngK4sZc82XIJTsV8sLjvHLup8z2mjv+Ol4yfV6fmNOyyMJnExuWAnZxJ4OlRkdHOVwlyOlxWWFVyT1fSUEhkxTIndpvpA6p6fb6Vp8mmmEXCSxPK8p5ghnqczkkClBQUHCorWpbu6V1Y9Mb+41S/j1FZjbvFKkaRDlmaCgyoADxqanjxoaUphDd2o8chv8AwNYF4ddxSHSnajZ1nNIsOqWl1pu0GSpUasEpxtl1UQd1ODjjG97eJtAEqkeomQfYAdNS1uXOkas/3jo7FZEo6xKI4fGeFwOx2PYK/tVOFfc2yDVdLT7v1dQ8beBpWMk3gHG3PaoHaafs0GH59jegWDBuQfOv9qGWa9xrjeREziEpx6pHKCYchqt/iM+S18l4SjJixmaSGhylPPdIk0kq1KsZsnJtfU8AUKIqjqhdw6rpGleoz2969mkwuHth9FGZDFkqFqIw+RsoNK5Wp2YafTS0l0vVtU9eguLNLtoTAlyfpXEfNz0LUMhTOuYitMy17cdj2LtE3yu5HqdgW7q3dDsCUFnN3mVYGe5LzL6Dm04usyb08RMsjNwK8ipIqAouEh9iiJzF/j6jdO6g2s2oQQjQdDQtMgzLbAMtWAqDXgR2g+XEhqGwbqGwnlOua24WFzla4JU0UmhFOIPYR5MQs7S/bUuHIDt+YDrkX3Eu4FhTC2jq3oZXiO8ylLzCrfEO26RWVfmxWm6J0Y347WhjSL3wH+ukHi6o+049WbqBvS30nd95p8mj6RdPHyvpZoA8rZoY28TV45a5R5FAHdiubD2dcartS0v01fVbVZOb9FDOUjXLNIvhXuzUzHysSe/D1dN4AVzWuINL4lW/fORaydG9wfMNtZaGq12WfsMU0sDePl7paAZnPZETrWAyjtuYEfehQSAVC+Xx6VljuybT9wya/b2lnWWoMJjrCqkrUItfD8WgPGlTwwzr3asN/t+PQbi6uyIqETCSkzMA1C7U8XxqkcK0HHFIaIxwJC8t+HsXbd+Zd1KQ5KPcAtOmG3pQcMUyGGkDSpLwaaSTa2N14MyEQQiDSC/nRbC5KUrtVFiGzkmpZLU7ikjtDsNbITrHyPp+cRTJTio48S+UcTl+KC2Na49PD3I28kl0N8G8MDSc/wCh5QNc9eDHhwCZjwGb4xC4vP63wsiNN4Pu+EcVrGkUCAc5bWstV02CfpPr5JRkC2i20o5sbqTFU06F6TYKpzyZlkjyKD1ymKpQVMPWrmn7lksdzjc0lvDLKJ2l5bCkYLEkBQPi5Kjl8DlKqacMbNX+3I73bR21HPNFEYFi5imrkLSuYn42ehz8RmBIrxxWK4icRNgtPBjuFZlwtlZuK2XAO5g8VyRmnJ15lKXmBymTpsW0pNpsk6EbGnjRjkCSqxF1WzN29jiJrh6CqpBd+4dw6dBunR77cqq2m3eiDmmjEI0ocl1VamtfCKAkBiRxAwl9A0DUJ9satZbcZl1G01k8oVUF1iKAIzNQUp4jUgEqAeBOGmcOa6d/3kub3z/odKgLalxM4jStlq0C3ZTFZq13eUuoOrMyrL5Zg296YsJw6yaDwEUlHCZCqGABN4BRNxzBenOl+qSyvb/eF2FZqhmQOwUsKmhK0JFSAeGLvt+Et1D1L1qKNLj1C1LKtCquUQsFNBUBqgGgqOOI78/MsYdv61cm+d2gbTM8iuavJo6vHPgLXJeOi4qax6GvcIhAybutQkQZBoEtQGMy+SbyLJo3a+1umv8A2yedD1MbTv33bb2O1bS2Wz2zY/4i/YEkTFGzAMx40kIWqsSe0jwxLiI3VYptSe93Rd3LXe473/D2KkAGIOuUlQOFUBajAAdgPilbGkWRDiDgvH7hR2duUZ2eoUzkHXZA965JQ+1Vayjx+5Mu3Xo1xjUohu7sEjUomr2yXThmbxynFwnxSt/XEepqTZE8qBtw6rq+p9RdCrBc2bjJbGFl59sB4i5IUOWQF2UF3zDgVIiJxpht/S9K03p7rdJ7a7Q57gTK3Iua+EKBmKBWOQE5UyniGBkAl5zsoO0dvLsp3qDqnLLYbdr2OBkTGubZISret3VNjObJm1Xf0mqvYs3x9H0xlX365GrV3ISj5NqkKR3Rm6aSSNe2td6bu/qXFLcafbR6dc80tCAWSqwyMHYHwlywFSFVa8ctSSZ/c9rqO0unEsUF/cSahb8oLMSFehljUopHiCBSaAszUFM1AAEKbDzUtFRqmbzGEdzTuUaBo8roufxU/VNdgrNQ6GFflDLGsR29k+Mzg9dovE0UkEBDwcIKHN/1emtpu2oLi4mj1XRNFhs1hkKvEyySZh8Xw04ClST3GmFfqG457eCGTTNZ1mW7aaMMsqsiZT8bxV4mtAB3iuHpcEE0/wB+X3akvIT0vmZjSfp+Uvp+n826MXyeTw8vk8vs8PDw8Olduon3W7fPfzZv+0+GZtgD3m68O7lw/wDZTCd+SPaz5oZxrt4jK/id31CpO7RPP6nds/iDWWOnYB3JOHUY8etIlR29gJIzRYgOGjoiZklwOVMyqYFVP5961sTctlqEscNrLPbl2KPGMwZSagkCpU07Qew9lRxx9NfS383vQndGytPu9S1+w0jWUtIUuLW8k5DxTKirIqtIFSZMwOSSMkMmUsEYlF4N+745w/5Ut1+zuw/3PqK9kd0fYLr0bf1YYv8AzK/l/wD+Mdvf52H52NVu/bb502aqTkG14r7kk5fs/I2OpndiBMV0VU3KKahgZCJU1VEQKYfAfKBvHwHpk9H5db6e9TNH3jqOmXsun2V3mlVYmLct0aJ2UU4siuXVajMygVFa417/ADW756Ldd/y7bs6TaBvjbVtr2saYUtnkvoRGbiGWO5hjkYMSscskKxSOAxRHL5WplMGl+1b3H26yiJ+E/I4x0zCUxkMwsblERD+NNduzVRVL/OUwh17KWXVfp1f2qXkOr2axSKCBIxhcV7mjlCSIfKGUHzY+SjWOkvUTRNSm0q70uaS4gcqzW7R3UJI747i2eWCVT3NHIynuOPy/dZ9xz/JLyU+ym1f3DrL95fT/APGdO9On9eI33db8/CL/ANC/9WD91n3HP8kvJT7KbV/cOj3l9P8A8Z0706f14Pd1vz8Iv/Qv/ViWFO7evPVeAi2zzhvyJjHrBi2ZOGzzK7U3L5miREAUQUPH+kokqBAMAAYTFAfAQ9nSb1zc21Y9Smlg1OxlgkkZlZZkPxiTQgGoIrQ8KHuwgtx9C+q8WrTzW2hanPbSys6skDt8clqMAKgitDUUPaO3G1fu6ud3+Une/s3sf9y6iPazbP2+19Iv9eIH3J9Xf+G9Y/y0vzcH7urnd/lJ3v7N7H/cuj2s2z9vtfSL/Xg9yfV3/hvWP8tL83Hks+2/zzfOkGaPEvciKuFCpJneUOXjmpTG/gFd9IItmTVP+U6ihCB/GPXFt3bYVSxvrag8jgn9QqTjsi6H9X5pBEm3NWDMaeK3dR+lmAUDzkgYsw9nntbXbiQ/n9+5ApRrHYbPWz1Sq0WOkWk0XPq3IOmr6ddT0pHqOoh3bJpWObokKxWXQZMyqFFZQ7lRNBO7/wB622uqul6VU2CPmZyCM7AEKFB4hRUniASacBQE72/ln/L/AKr03mm3jvIRpua4g5MUCsH9XiYhpDI61QzOVUARsyogYZ2MhCPu6WGNwcHRgwdGDCfbn/rqYv8A7bujf48tOmRbfyouv43H+4OF7c/zRtv4NJ+/GHBdLfDCwdGDCodN7PvGLWd15U8j7g4tMzrHJXNF87h7DMPfjr9hqkjmquYy9qzJq/VUQSnXkKk1Fso6BQ8UVuZFgdsiscnV9suoeuWGlWGjW4jXT7KfmFQMvPpJzQshH7INagfGrVwxAxRb3p/ot/ql9rFwXa/vIeWGJryax8stGD+0RShNctKLQE45ZH9grgE04rl4zOKxbFZFeah7lJby1nEG+yL3yJbqMzT8bJPGMnXYSIdx7hVmeFRj/i0WpynMmd8mm9Lnv1Y3a2vffYePJlKCAr9DkPHKQCGYg0OctmrwqFJXGCnSzaq6H9ylJMxYOZw302ccMwJBUAioyBctO0FgGxNbUOA/H+8ZJrWf1mk1Km3vU8Cm+PCu7SlXb6JrsTSpPP3WbMCur3b3zi8WQYWtufTSK7l/MoYhROcTfC6rNjuvVrXULe7nlkktYLtbjkBuXEXEgkNEQZFzMONE4dwxZL3a2lXNhPaQxxx3U9qYOcV5koQoYxV3JdqL2Vf4TiJvDTsz4lxcs12uOh3+08pp604fWOOkMGvQ8WaEpGPx9PaVi30euwDdy9jk4K5mQOUUjl8zOMN7oB1jrPnb6f3J1H1PXYIrazhjsIo7prg8knM8xcsjsxocyeXvbxUACqsDt3p3puiTSXF3LJfSvbLbjmgZUiCBXRV4jK/k7l8PGrM0hODXbkyHgfVtfoOe2e+XfP8ATNFUuUFTdGn5GxV7PK6VhHCxpcJAvHy9ZcGZWAr54eYCPbSr9Fdsi+VdHYpODxG6N46huqe3u7xIoruCHIzxqFaRqmrswGbiuUZMxVSGKhc5GJbbO0NP2vBcWto8slpNNnVJGLLGtBRFUnLwapz5QzAqGLZQcYfnR25K7zPtWC6VDbdqvHDWuOru3Dn+jZC4YtZdrFXZhGsZqNEjgEVGipCxCRUF2yyXkQWcoqEVIsX0u3a28pttwXdlJa295p94E5kcwJBKElTw7e01BB4gEEU49e5toQ7intbyO5ns7+0LcuSKgIDgAjzdgoQRwJBBrwj5mXZ2LE8icT5E8heafJblpMcepWXsuV1bYpGMewVftMomw8kwRU5pF6mRo9i2j30mxmwrPWDQ6ihk0RSUlr3qLzNHutH0jTLLT47xQsrQghmUV4dw4gkVNaKzACpqImy6fZNXttX1bUry/ktGLRLKQVVjTj3ngQDQUqVWpoKGdHNjiPG80sZ/Y7JaxquMojZoqeUuGQ2RzXLI5j27aRipysSApqlZS1fskFLuUF2rtNdt6vpLGSOZEpRq+2dwPtrUvvFLe3uTkK5JlzKDUFWHeGVgCCKGlRUVxZ9yaCm49O+73nnthnDZ4mKsRQhlPcVZSQQaitDQ0xuXE3jLn3Djj1m3GzLXdikKNmTCZaREjbHrKQsck5sdnm7jOyUs6jY6IjzuZCw2J2t5UGyCSZTgQhQKUOsbX9bvNx6vNrV+EF1OQSEBCjKqooAJJoFUDiSe/GRoOi2m3tJh0axLm1hBALEFjmYuxJAAqWYngAO7Ef8Amt27aVzhl6S4u288ncvrVdZycXbKHjGvzNTpemxTxdk5ZsrbVJFOdqhFo9RBcgvGkeg/ct3Rk1ljAi2FCW2zvC52xHKLa0sZ53IKPNCHeIioJRhlbjw4FioIqBxasVuPaVtuWSM3N1ewQoCGSGUqkgNCA6nMvDjxChiDQngtOSyXY/7ZUljDbEvw0QDCIZOVpJne46ZsCGvozThumgvKqacpIrWh+RT0imGOdLLwwGKXwZgUpShIJ1O3smpHU/XXMhFChC8nL5OVTKP7QAf/AEsYD9NdmPpw031NBGDUOC3Nr5eZXMf7JJT/AEcdp4QdvbOuCYaGhQda37UWF5Wgyx7Tb9ENdUqNFQrVVEIapMmEXX4OPbPVlCissDIHaqLZskoqcqBRGN3Pu683TyWu7e0geINUwR5M5J+M5JZiR3CtASxAFcSO2tp2e2OcLWe6nWUrQTSZ8gA7FACgV7zSpAAJNMZniJwbp3EiP5KxkLdbLdGnJfdb/uVjCVSRgnFef6Cgi3ka7CPoFw3fFZskkf6l0Ciboph8wGKYAEOrcO6LncD2TyxpG1laxwLTxZhH2MwbhU944jHboG2bfQUvEjkeRb26kmavhymTtUFeNB3HtxzDgx20Klwr0LX9je7vuHInYNojoCAtF+2qy/OCTRrVYUMeFi27hyL6cfOEUwSRM4fyDswN2qKaJUSlOCmdujetxuWzt9NW1tbPTrYsyxwrlGZvjE0oo7zRVHEkmvCmDtnZsG3Lu41Frq5u9QuQqs8zZjlXsHeT3CrMeAAFONfz5ldpXiJzw1+mbPyCjL7J2ClUslDQh6tcVKtX56DQnn1hZ/OEjFgeZVeMHUo6TSVZvWRhQcnKr6okbih+7b6gbh2rp0um6Q0Swyy8wlkzsrZQpy1NKEAEgg8QKUqam4th6BujUI9R1ZZWljjyUV8qsuYsM1BWoJNCCOB41oKcN0PsDdtaz5JZM0oWLnyKenPiQ0frFetd1tmh1leInGcsu5hZDRLPamZDyzVBZm4TOiZAW7gfAgCmj5JSz6s70g1BL27ufWIlzViZUWNgVIowjVTw4Eca1HbxNYy76V7OnsHsrW29XlalJVZ2kWjA8DIzDiKg8KUPZwFGR6NxfyDasHjuOe6Q0ltObtYamRUunfp2TUsFudUb4sWiLFaZ6tr1x4+sTmSiUnrtdH3dNw6E5hTApvJ1TLPXNR0zVTrOlsttelnI5ajKgetVVWzAKASoBrQd/fi4Xmi6fqOljSNTU3NmFQHOxzOUpRmZcpLEgEkUqe7G26li9A17KrBj9vg2jypTdZfVpsmYigvK+DiFdQbGZr8iRZKThrBCN3QnZP2q6D1sqAHSVIf4XWPY6ld6ffpqNuxFwrhvM3EMQw7CrU4qQVI4EEYyL7TrXULF9PuFBt2Qr514FQVPaGFeDAgg8QQcLZ4v9oWjcYqPymbxfIveb9uHK3MZjMbZyFuc8k4vFRZu65NwMLPU07VROWQsEEtLJPCOncm6XFaPalRUbkTEDXTXOoV1rl1YGSztItLsJxIlui0RyGVmV68CrUIoFAozVBrinaJsC20W2vgl5dS6nfQmNp3bxoCrKrJTjmWoNSxNVWhFMN96XmL/AIOjBg6MGDowYOjBg6MGDowYOjBg6MGDowYOjBg6MGDowYOjBhStw9y/fbY75vif4w/d36F6fn+cHx77r+3Fp5vQ9P8A9s/Fvqf0vU/tnqf0fgdMO2ze665+Nk++Y/k5a8g/61fg4fpxQbjL7zLf4uf7nk+Vmpzx/q0/p/RhtXS8xfsHRgwdGDB0YMHRgwdGDB0YMHRgwdGDB0YMHRgwdGDB0YMHRgwdGDB0YMHRgwdGDB0YMHRgwdGDH//Z\" />\n" +
            "\n" +
            "<body>\n" +
            "</html>";

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        logger.info("HttpsNettyHandlerTest--请求消息接受开始");

        HttpRequest request = (HttpRequest) e.getMessage();
        String uri = request.getUri();
        if (uri.contains("/favicon.ico")) return;
        logger.info("uri:" + uri);

        int contentLength = (int) HttpHeaders.getContentLength(request);
        ChannelBuffer channelBuffer = request.getContent();
        byte[] contentByteArray = new byte[contentLength];
        for (int index = 0; index < contentLength; index++) {
            contentByteArray[index] = channelBuffer.getByte(index);
        }

        String charset = "UTF-8";
        String requestContent = new String(contentByteArray, charset);
        logger.info("收到报文：" + URLUtils.urldecode(requestContent, charset));

        logger.info("响应报文：" + URLUtils.urldecode(responseContent, charset));

        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
        ChannelBuffer buffer = new DynamicChannelBuffer(2048);
        buffer.writeBytes(responseContent.getBytes(charset));
        response.setContent(buffer);
        response.setHeader("Content-Type", "text/html;charset=" + charset);
        response.setHeader("Content-Length", response.getContent().writerIndex());
        Channel ch = e.getChannel();
        // Write the initial line and the header.
        ch.write(response);
        ch.disconnect();
        ch.close();

        logger.info("HttpsNettyHandlerTest--请求消息响应结束");

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        Channel ch = e.getChannel();
        Throwable cause = e.getCause();
        if (cause instanceof TooLongFrameException) {
            sendError(ctx, BAD_REQUEST);
            return;
        }

        cause.printStackTrace();
        if (ch.isConnected()) {
            sendError(ctx, INTERNAL_SERVER_ERROR);
        }
    }

    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);
        response.setHeader(CONTENT_TYPE, "text/plain;charset=UTF-8");
        response.setContent(ChannelBuffers.copiedBuffer("Failure:" + status.toString() + "\r\n", CharsetUtil.UTF_8));

        // Close the connection as soon as the error message is sent.
        ctx.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
    }
}