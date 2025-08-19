package io.avaje.jex.ssl.cert;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.function.Supplier;

import io.avaje.jex.spi.ClassResourceLoader;

public final class Client {
  static ClassResourceLoader loader = ClassResourceLoader.fromClass(Server.class);

  public static final String SERVER_CERTIFICATE_AS_STRING =
      """
  	-----BEGIN CERTIFICATE-----
  	MIIDpjCCAo6gAwIBAgIUKK29nJVFCs8SjBqcvxrg7boyem8wDQYJKoZIhvcNAQEL
  	BQAwQjESMBAGA1UEAwwJbG9jYWxob3N0MQswCQYDVQQGEwJFUzEQMA4GA1UECAwH
  	R2FsaWNpYTENMAsGA1UEBwwEVmlnbzAgFw0yMjA3MDYxMTQyMDdaGA80MDA1MDMx
  	MjExNDIwN1owQjESMBAGA1UEAwwJbG9jYWxob3N0MQswCQYDVQQGEwJFUzEQMA4G
  	A1UECAwHR2FsaWNpYTENMAsGA1UEBwwEVmlnbzCCASIwDQYJKoZIhvcNAQEBBQAD
  	ggEPADCCAQoCggEBALtW247iPVAuCcQByuqgj8tSzJcwVqCmheT6ld0Xe7DYoLOL
  	EsjilB/jgG9aBEBfYJ2h74K7SIdqiSDz4rgUuJUzhZnJo5d3n3wT9Wb2AZcsqFce
  	JK0UNBKe2/1b01dFWtQFW4zHC/JM/Gp0dMTy1Vt1Zf/3SmQjSD/KzgJf4m2O/GOP
  	3iRFsCSPC4CU3TZCDmI5/qRr4icJCY5s3gJ+RT+edfsvtdkfAO4hK/p+37RrwHax
  	nyFLoAzYdJMcnDX/+V7Ez2y7jkTkcUk2gKG+3dpio2XqAE9pXcXa4kYk0NL9Vw6L
  	C2QMefFKHLDqLWx/bfQXpbULFawldETDbuLVe7UCAwEAAaOBkTCBjjAdBgNVHQ4E
  	FgQUiiPTBoFstcGbb0zYWsM/ZwupRRYwHwYDVR0jBBgwFoAUiiPTBoFstcGbb0zY
  	WsM/ZwupRRYwDgYDVR0PAQH/BAQDAgWgMB0GA1UdJQQWMBQGCCsGAQUFBwMBBggr
  	BgEFBQcDAjAMBgNVHRMBAf8EAjAAMA8GA1UdEQQIMAaHBH8AAAEwDQYJKoZIhvcN
  	AQELBQADggEBAGvqUrtYWZpKBJNYL4UVLnm2+dQl33l8BH7PhU6YvMufThDCVjOw
  	IJ7ezOReDlCAmytQD7ChKpsJrAOBzKRdrifL0f88psbE83+6Ys/s/1rHMq282p/S
  	WPRiZDVO8Mw2ra9v9b6cprW5phHJkp7TiIBP82A+v19lt3R+vE4HZ91ZyioNqMzf
  	Aqvd5gfxHexpilgil0osF0o/8ajSnLiBfWI82Lz/1JB+xUMYW91ahRgt13/54h13
  	eL70steoAmx55he3pQaaeRZKzI1nLxsrTkjs055jDn0G/yj1L6kY3OeVFg3AhETJ
  	sg+yATMTef2Qskr4dgzb1LJkC9meaU2TFwk=
  	-----END CERTIFICATE-----
  	""";
  public static final String SERVER_PRIVATE_KEY_AS_STRING =
      """
  	-----BEGIN PRIVATE KEY-----
  	MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC7VtuO4j1QLgnE
  	AcrqoI/LUsyXMFagpoXk+pXdF3uw2KCzixLI4pQf44BvWgRAX2Cdoe+Cu0iHaokg
  	8+K4FLiVM4WZyaOXd598E/Vm9gGXLKhXHiStFDQSntv9W9NXRVrUBVuMxwvyTPxq
  	dHTE8tVbdWX/90pkI0g/ys4CX+Jtjvxjj94kRbAkjwuAlN02Qg5iOf6ka+InCQmO
  	bN4CfkU/nnX7L7XZHwDuISv6ft+0a8B2sZ8hS6AM2HSTHJw1//lexM9su45E5HFJ
  	NoChvt3aYqNl6gBPaV3F2uJGJNDS/VcOiwtkDHnxShyw6i1sf230F6W1CxWsJXRE
  	w27i1Xu1AgMBAAECggEAfPI7UZr3BckO3lnLup0ICrXYmmW1AUTPPJ8c4O7Oom55
  	EAaLqsvjuzkC6kGBYGW8jKX6lpjOkPKvLvk6l0fKrEhGrQFdSKKSDjFJlTgya19v
  	j1sdXwqAiILHer2JwUUShSJlowkGoL5UA7RURR8oye0M8KFATnVxtIpQyCinXiW/
  	LkDuqUr8MIbu6V/KcoSOLfJyTWyuwSRPHuFKhv154UAqaTkSPbf2mCTa9hH5Tb4f
  	Lfzy9o3Ux4ieZceG28De+SmC7uMzbBs1stowOuDmFg3znI/1Br/sQEAXPFngDe3s
  	soDD2PbLo7/4SPBNgl5vygf7jhvxHPY3DTUXOxLSgQKBgQD4EzKVTx/GpF7Yswma
  	oixidzSi/KnHJiMjIERF4QPVfDNnggRORNMbPnRhNWSRhS7r+INYbN4yB/vBZO5I
  	IIqowdJbLjGbmq91equP0zzrP2wCjqtFK6gRElX7acAWY5xTesIT5Fa1Ug++dFLS
  	MxCZKL6JMZaHJzZVzXugaltMsQKBgQDBUvPSaDnIBrZGdNtAyNMxZyVbp/ObIKW1
  	TvCDX2hqf+yiTVclbZr5QkwCE3MHErfsKlWU01K9CtzsQh4u9L5tPaeFlvm6iZq6
  	ktbflNvI+z+qEW3JbROR4WwwbmWFvKRLBA0OQom7tGuNnNyRtkDFxlkFJPcD6Eff
  	ZEq+ewrQRQKBgQCV7URM6J0TuJN58/qB8jFQ8Spmtr0FFw91UzLv6KYgiAepLvLb
  	Os07UeuUNGiragqJoo//CQzgv+JvZ0h7Xu9uPnWblbd1i28vWQwGyGuw4Yutn/vy
  	ugfBCYvdfnQRE/KOoUpaK04cF5RcToEfeK03Y2CEGewXkqNMB/wHXz/+gQKBgE8Y
  	34WQ+0Mp69375dEl2bL23sQXfYZU3zfFaoZ1vMUGPg1R03wO0j91rp+S0ZdtQy8v
  	SwCvTcTm8uj/TFYt8NPFTAtOcDKwJkx708p6n0ol8jBlHSQyqrUfJCLUqFkFi7rd
  	l3HkK3JPKUoxidVcWjgRJU8DhsVkfjOaVzKEKTJ5AoGARBwn7gt2H35urQ6/U3nJ
  	hFjOVn01F5uV0NvRtRDCsAIUMeA2T4pwALUUIqlA9HmpwYgLeG4bZ+SkhNpy70N/
  	qcufT1DeM+q3H5zFPANyjcqVaqa6KUnttvi/lhxMdRb6GsA9TzzHzY1P9ovpIOCK
  	IS639NPzxpI0Ka+v6t+nFEM=
  	-----END PRIVATE KEY-----
  	""";
  public static final String CLIENT_CERTIFICATE_AS_STRING =
      """
  	-----BEGIN CERTIFICATE-----
  	MIIDmDCCAoCgAwIBAgIUdWY83fnUuYRDmDnHi34wkeG+yA4wDQYJKoZIhvcNAQEL
  	BQAwUjEPMA0GA1UEAwwGY2xpZW50MQswCQYDVQQGEwJFUzEPMA0GA1UECAwGTWFk
  	cmlkMQ8wDQYDVQQHDAZNYWRyaWQxEDAOBgNVBAoMB0phdmFsaW4wIBcNMjMwMTEw
  	MTEwNTE0WhgPMjEyMjEyMTcxMTA1MTRaMFIxDzANBgNVBAMMBmNsaWVudDELMAkG
  	A1UEBhMCRVMxDzANBgNVBAgMBk1hZHJpZDEPMA0GA1UEBwwGTWFkcmlkMRAwDgYD
  	VQQKDAdKYXZhbGluMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsCUt
  	UOTWxQ3zbVXK8FPFh4DsDCphoUt/k6v8n4miZWxfw45VqsSk5JktmFhqSsmerh0N
  	cQZ7rji69LK/dr4wfZJjWLGlyNDJnT1W/PP3HaGErJxDl/NqLjl+xULXsp7+/SP7
  	Jz6QcEKmDYOyQND79MaYXlhkCLtt/RslfIP1YQ4AFCGcw4z/cGERuMtcLY8FFT+N
  	U4OD26AZX4fAQ+fQRAdALzp63wCnWiYyQ+0Nqeq4wDM+HYlAsUbwSwiJSseIVn2u
  	nn1kQq45TUcL8HUuVGr9CF8PyvkOLxbdzC0q43MfPDck7CgqR2YG9XHrca9cT6c+
  	zE+BGhjzOjlAxUCYqwIDAQABo2QwYjAdBgNVHQ4EFgQU3MAhBUHI6S9obysrX38v
  	HiGLmIcwHwYDVR0jBBgwFoAU3MAhBUHI6S9obysrX38vHiGLmIcwCwYDVR0PBAQD
  	AgeAMBMGA1UdJQQMMAoGCCsGAQUFBwMCMA0GCSqGSIb3DQEBCwUAA4IBAQBVjsXw
  	P4ZbUt5XqN8Iy30YqBi90OtfcmWxVnuc7O/HU2ue+PLM3rRyKYVgwY6G/HoRAibq
  	HsLnGa/2jT+qzri68CKfjE/wmBIAgzaKF4XmhOxIxQjTUuxbVd4BmrDmGqsMgWoQ
  	5XnYvyBQJ9LTnvvxPk4UvhUjm1/6RbPFqqxVenTUpYS4U3JTv1c9ddu9VSA8ebT4
  	BGBVq2iwgTm38xN9C6eS/xGCdLXGIaEQvmfgAPi1Nmw32KrLJfL3oz9bWdYhp9Hg
  	fZg2Pug5bLDqy8ktyTDdM+q4d+wd3XpKzuLvCIr2q03vrT9j+dMIEOTaqxWQAYiH
  	CYGXrU6Ry61UJSeroQAxAA==
  	-----END CERTIFICATE-----
  	""";
  public static final String CLIENT_PRIVATE_KEY_AS_STRING =
      """
  	-----BEGIN PRIVATE KEY-----
  	MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCwJS1Q5NbFDfNt
  	VcrwU8WHgOwMKmGhS3+Tq/yfiaJlbF/DjlWqxKTkmS2YWGpKyZ6uHQ1xBnuuOLr0
  	sr92vjB9kmNYsaXI0MmdPVb88/cdoYSsnEOX82ouOX7FQteynv79I/snPpBwQqYN
  	g7JA0Pv0xpheWGQIu239GyV8g/VhDgAUIZzDjP9wYRG4y1wtjwUVP41Tg4PboBlf
  	h8BD59BEB0AvOnrfAKdaJjJD7Q2p6rjAMz4diUCxRvBLCIlKx4hWfa6efWRCrjlN
  	RwvwdS5Uav0IXw/K+Q4vFt3MLSrjcx88NyTsKCpHZgb1cetxr1xPpz7MT4EaGPM6
  	OUDFQJirAgMBAAECggEADdQNV7Fvbu7mcmnu0akx865aWaYmHfyIWnaBEaFDf4Tf
  	i8Gr1gk0DMI9wx0F0zM64t5jBMGGiinn+3fg8hiCRAlvBTKFGlvRyCddoeQhPVFF
  	0is+Xzp71n8rBZ92wY4b5JGjkPQncLi6worZPp9peFDy+00jJVBZlSpBaiIN7H2E
  	iZQYUMI07u6xJW/EUE6X9g3AhgV9QMxfJawn8AWHXR8+9iNsOb9hlVUWBPwR7xb5
  	4KqB/89UFp/40tEDeKz9/MMsH5FjNCNPCaLADJS2Xy1Q1icV6V42HsaZm9vZUL+J
  	dru6OwEo6iJhWKjkBaWvVl4HuOPrrUP9sLSN6g6PCQKBgQDXih7xgHF35yDPvnNx
  	fqqxfRO+PMHq1se2tOhAdeDmdStUyl/u1NwJ9BE9Fb/lbdulYFfZJtef2TmeX31x
  	DaQWXrg4Pai2pnCcSfItogWJSFrg6dphbABwVslTvWw2ikB6hN2jmUaReM0atW2S
  	YUVWD0JFMsf4IimgAcGPgebprQKBgQDRNfB2k6NhqgKBXKrshT/3No/kMnhkNl9H
  	i/UmiCUYvw5E/L4q2wrsehnERAPpod3EoHjkYCmY/BK4oRCtkz6t1nnWX1zGabY3
  	Nn4Ie+BMCYp2NLa7yGZ0sk1rrtlgZBoaR1ZF0+HADPpffELPD6HzvUQuPyN+wlA0
  	SWwq8DuGtwKBgBxyxIbHlzJmNTR2RLJ0L39hrNttFYMzegSpeAYaCOciC+gTFfpl
  	6ez+Y9AWMM/NYjI/txiYQdl9SFeY7uufC0tQkSwLJ1uEOFTIhch0HBr0i9onw4Uc
  	RiqNqeD9nWzNbpk9NCvFrUTCFwAxdhbd89LaDLspaq9bgvb1hGC2mo25AoGBAKVP
  	ks+Pf3Unik0/tQupis7DvVVakAjXcdgt/itRPsbcCOF4OKfSZ0JOhNexysmsjnjV
  	OFF0rsnkvMJI+s284LUqGSHMPpnFZCciltoLUEOk8lTO+GlPQ64ISebBxaBF2N5U
  	6hXJA8PmPVx/6qaEurrHHf3RBDIgRpHaRm9zXgXnAoGASlEFHkUKwf8G3AePstzk
  	sHoxJiKMTq2qFb/NTVE4z4+pc03uhxno79+R4aV4JD0dK1gyRaX5/TCwdvI5smS3
  	Vfl5JN+HiO0zClecR8N83arOLka2prJ3ZjjCy2JgZKRXZQ/vcsTKnvh3DIFyR/NZ
  	OKM5x3IGChzxEZLumfedQX4=
  	-----END PRIVATE KEY-----
  	""";
  public static final String WRONG_CLIENT_CERTIFICATE_AS_STRING =
      """
  	-----BEGIN CERTIFICATE-----
  	MIIDmDCCAoCgAwIBAgIUD0yMp2hSck6P1TtqpbVvf1QlJMswDQYJKoZIhvcNAQEL
  	BQAwUjEPMA0GA1UEAwwGY2xpZW50MQswCQYDVQQGEwJFUzEPMA0GA1UECAwGTWFk
  	cmlkMQ8wDQYDVQQHDAZNYWRyaWQxEDAOBgNVBAoMB0phdmFsaW4wIBcNMjMwMTEw
  	MTE0MzM0WhgPMjEyMjEyMTcxMTQzMzRaMFIxDzANBgNVBAMMBmNsaWVudDELMAkG
  	A1UEBhMCRVMxDzANBgNVBAgMBk1hZHJpZDEPMA0GA1UEBwwGTWFkcmlkMRAwDgYD
  	VQQKDAdKYXZhbGluMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvt3f
  	vHU1vVoZFu5pV53IXjSP7FoRi6mB27RASyOTXZGCdKTmRZxlgUF7yrMjse99zkys
  	GR4csPE62vxDB5SHiaLdCVDWFUNmvENJ+Om6v4SnUrVju/1OUDthsTBXe6t7N0Ou
  	ihPxN5tZKumdDaB56djIXkEfmPFFc/7vRC9cqYISWvKtFT2bkBwzNkcUzTlR05WL
  	8m5napdl8SQ3/Gza+iVjDtkBDvKs4nlG+QmhT0U4+5B1vah1doKfv+Sn2CAfoTs0
  	aIMuHAcdApLR4IVEIADPhNb9pePurXChFHGq7kY90g+wh69rNVsi4uq8HwPSTaQe
  	YhsTebk71irMquoSMwIDAQABo2QwYjAdBgNVHQ4EFgQUeZ640SK+L1/GPQIis8mz
  	bHOOQvYwHwYDVR0jBBgwFoAUeZ640SK+L1/GPQIis8mzbHOOQvYwCwYDVR0PBAQD
  	AgeAMBMGA1UdJQQMMAoGCCsGAQUFBwMCMA0GCSqGSIb3DQEBCwUAA4IBAQBNinqE
  	9Xltwk+khvbRmkF/AIbXMIIFpgGjUWmlg42aUmba+OdQKjHbChiSZHpsue6o/Abj
  	AgPpb4xH9AacQVM2yFTh/o9UeRwAJtjHrSzIgkBTy2YOM6TFXi2M6a6fBWuEuYQC
  	jB0std0HNK0ln2MqFKJn3IMk6oiX3XslTXbcTOP8S/T2fj4bc3C4kBZWjUj3qreD
  	QqzvaWOpVUt7a/slICZ5fVII0vn7EnaNvjsZq9ilBs9MuBH92LNJ0nIO9rhw94TQ
  	xYyJ1RUBugQrcnpx6xMW3cIUuv/IXu14X+5wEOw21udKaafen5WYVqEkVBW12bgP
  	0I8c8C6x8S6P4eDO
  	-----END CERTIFICATE-----
  	""";
  public static final String WRONG_CLIENT_PRIVATE_KEY_AS_STRING =
      """
  	-----BEGIN PRIVATE KEY-----
  	MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC+3d+8dTW9WhkW
  	7mlXncheNI/sWhGLqYHbtEBLI5NdkYJ0pOZFnGWBQXvKsyOx733OTKwZHhyw8Tra
  	/EMHlIeJot0JUNYVQ2a8Q0n46bq/hKdStWO7/U5QO2GxMFd7q3s3Q66KE/E3m1kq
  	6Z0NoB56djIXkEfmPFFc/7vRC9cqYISWvKtFT2bkBwzNkcUzTlR05WL8m5napdl8
  	SQ3/Gza+iVjDtkBDvKs4nlG+QmhT0U4+5B1vah1doKfv+Sn2CAfoTs0aIMuHAcdA
  	pLR4IVEIADPhNb9pePurXChFHGq7kY90g+wh69rNVsi4uq8HwPSTaQeYhsTebk71
  	irMquoSMwIDAQABo2QwYjAdBgNVHQ4EFgQUeZ640SK+L1/GPQIis8mzbHOOQvYwHw
  	YDVR0jBBgwFoAUeZ640SK+L1/GPQIis8mzbHOOQvYwCwYDVR0PBAQDAgeAMBMGA1Ud
  	JQQMMAoGCCsGAQUFBwMCMA0GCSqGSIb3DQEBCwUAA4IBAQBNinqE9Xltwk+khvbR
  	mkF/AIbXMIIFpgGjUWmlg42aUmba+OdQKjHbChiSZHpsue6o/AbjAgPpb4xH9Aac
  	QVM2yFTh/o9UeRwAJtjHrSzIgkBTy2YOM6TFXi2M6a6fBWuEuYQCjB0std0HNK0l
  	n2MqFKJn3IMk6oiX3XslTXbcTOP8S/T2fj4bc3C4kBZWjUj3qreDQqzvaWOpVUt7
  	a/slICZ5fVII0vn7EnaNvjsZq9ilBs9MuBH92LNJ0nIO9rhw94TQxYyJ1RUBugQr
  	cnpx6xMW3cIUuv/IXu14X+5wEOw21udKaafen5WYVqEkVBW12bgP0I8c8C6x8S6P
  	4eDO
  	-----END PRIVATE KEY-----
  	""";
  public static final String CLIENT_P7B_CERTIFICATE_AS_STRING =
      """
  	-----BEGIN PKCS7-----
  	MIIDyQYJKoZIhvcNAQcCoIIDujCCA7YCAQExADALBgkqhkiG9w0BBwGgggOcMIID
  	mDCCAoCgAwIBAgIUdWY83fnUuYRDmDnHi34wkeG+yA4wDQYJKoZIhvcNAQELBQAw
  	UjEPMA0GA1UEAwwGY2xpZW50MQswCQYDVQQGEwJFUzEPMA0GA1UECAwGTWFkcmlk
  	MQ8wDQYDVQQHDAZNYWRyaWQxEDAOBgNVBAoMB0phdmFsaW4wIBcNMjMwMTEwMTEw
  	NTE0WhgPMjEyMjEyMTcxMTA1MTRaMFIxDzANBgNVBAMMBmNsaWVudDELMAkGA1UE
  	BhMCRVMxDzANBgNVBAgMBk1hZHJpZDEPMA0GA1UEBwwGTWFkcmlkMRAwDgYDVQQK
  	DAdKYXZhbGluMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsCUtUOTW
  	xQ3zbVXK8FPFh4DsDCphoUt/k6v8n4miZWxfw45VqsSk5JktmFhqSsmerh0NcQZ7
  	rji69LK/dr4wfZJjWLGlyNDJnT1W/PP3HaGErJxDl/NqLjl+xULXsp7+/SP7Jz6Q
  	cEKmDYOyQND79MaYXlhkCLtt/RslfIP1YQ4AFCGcw4z/cGERuMtcLY8FFT+NU4OD
  	26AZX4fAQ+fQRAdALzp63wCnWiYyQ+0Nqeq4wDM+HYlAsUbwSwiJSseIVn2unn1k
  	Qq45TUcL8HUuVGr9CF8PyvkOLxbdzC0q43MfPDck7CgqR2YG9XHrca9cT6c+zE+B
  	GhjzOjlAxUCYqwIDAQABo2QwYjAdBgNVHQ4EFgQU3MAhBUHI6S9obysrX38vHiGL
  	mIcwHwYDVR0jBBgwFoAU3MAhBUHI6S9obysrX38vHiGLmIcwCwYDVR0PBAQDAgeA
  	MBMGA1UdJQQMMAoGCCsGAQUFBwMCMA0GCSqGSIb3DQEBCwUAA4IBAQBVjsXwP4Zb
  	Ut5XqN8Iy30YqBi90OtfcmWxVnuc7O/HU2ue+PLM3rRyKYVgwY6G/HoRAibqHsLn
  	Ga/2jT+qzri68CKfjE/wmBIAgzaKF4XmhOxIxQjTUuxbVd4BmrDmGqsMgWoQ5XnY
  	vyBQJ9LTnvvxPk4UvhUjm1/6RbPFqqxVenTUpYS4U3JTv1c9ddu9VSA8ebT4BGBV
  	q2iwgTm38xN9C6eS/xGCdLXGIaEQvmfgAPi1Nmw32KrLJfL3oz9bWdYhp9HgfZg2
  	Pug5bLDqy8ktyTDdM+q4d+wd3XpKzuLvCIr2q03vrT9j+dMIEOTaqxWQAYiHCYGX
  	rU6Ry61UJSeroQAxAA==
  	-----END PKCS7-----
  	""";
  public static final String KEYSTORE_PASSWORD = "password";
  public static final String CLIENT_PEM_FILE_NAME = "test-certs/client/cert.pem";
  public static final String CLIENT_P7B_FILE_NAME = "test-certs/client/cert.p7b";
  public static final String CLIENT_DER_FILE_NAME = "test-certs/client/cert.der";
  public static final String CLIENT_P12_FILE_NAME = "test-certs/client/cert.p12";
  public static final String CLIENT_JKS_FILE_NAME = "test-certs/client/cert.jks";
  public static final String CLIENT_PEM_PATH;
  public static final String CLIENT_P7B_PATH;
  public static final String CLIENT_DER_PATH;
  public static final String CLIENT_P12_PATH;
  public static final String CLIENT_JKS_PATH;

  static {
    try {

      CLIENT_PEM_PATH =
          Path.of(loader.loadResource(CLIENT_PEM_FILE_NAME).toURI()).toAbsolutePath().toString();
      CLIENT_P7B_PATH =
          Path.of(loader.loadResource(CLIENT_P7B_FILE_NAME).toURI()).toAbsolutePath().toString();
      CLIENT_DER_PATH =
          Path.of(loader.loadResource(CLIENT_DER_FILE_NAME).toURI()).toAbsolutePath().toString();
      CLIENT_P12_PATH =
          Path.of(loader.loadResource(CLIENT_P12_FILE_NAME).toURI()).toAbsolutePath().toString();
      CLIENT_JKS_PATH =
          Path.of(loader.loadResource(CLIENT_JKS_FILE_NAME).toURI()).toAbsolutePath().toString();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  public static final Supplier<InputStream> CLIENT_PEM_INPUT_STREAM_SUPPLIER =
      () -> {
        try {
          return new FileInputStream(CLIENT_PEM_PATH);
        } catch (FileNotFoundException e) {
          throw new RuntimeException(e);
        }
      };
  public static final Supplier<InputStream> CLIENT_P7B_INPUT_STREAM_SUPPLIER =
      () -> {
        try {
          return new FileInputStream(CLIENT_P7B_PATH);
        } catch (FileNotFoundException e) {
          throw new RuntimeException(e);
        }
      };
  public static final Supplier<InputStream> CLIENT_DER_INPUT_STREAM_SUPPLIER =
      () -> {
        try {
          return new FileInputStream(CLIENT_DER_PATH);
        } catch (FileNotFoundException e) {
          throw new RuntimeException(e);
        }
      };
  public static final Supplier<InputStream> CLIENT_P12_INPUT_STREAM_SUPPLIER =
      () -> {
        try {
          return new FileInputStream(CLIENT_P12_PATH);
        } catch (FileNotFoundException e) {
          throw new RuntimeException(e);
        }
      };
  public static final Supplier<InputStream> CLIENT_JKS_INPUT_STREAM_SUPPLIER =
      () -> {
        try {
          return new FileInputStream(CLIENT_JKS_PATH);
        } catch (FileNotFoundException e) {
          throw new RuntimeException(e);
        }
      };
}
